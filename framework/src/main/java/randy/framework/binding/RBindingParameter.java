package randy.framework.binding;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import randy.framework.model.RMapFile;
import randy.framework.model.RSession;

/**
 * Construit les arguments d'une méthode de controller à partir de la requête HTTP.
 * Les noms des paramètres Java servent de noms de paramètres HTTP :
 * il faut compiler avec l'option -parameters (configurée dans les pom.xml).
 */
public class RBindingParameter {

    /**
     * Un argument par paramètre, selon son Kind.
     * Objet seul → pas de préfixe (?nom=..) ; plusieurs paramètres → préfixe par nom (?personne.nom=..).
     */
    public Object[] bindAll(Parameter[] params, ApplicationContext ctx, HttpServletRequest req) throws Exception {
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            args[i] = switch (RTypeResolver.resolve(params[i])) {
                case APPLICATION_CONTEXT -> ctx;
                case SESSION -> buildSession(req);
                case PRIMITIVE -> convertValue(
                        req.getParameter(params[i].getName()),
                        params[i].getType());
                case ARRAY -> bindArray(params[i], req);
                case LIST -> bindList(params[i], req);
                case MAP -> bindMap(params[i], req);
                case FILE -> bindFile(params[i], req);
                case OBJECT -> bindObject(
                        params[i].getType(),
                        params.length == 1 ? "" : params[i].getName(),
                        req);
                default -> throw new IllegalArgumentException(
                        "Type non supporté : " + params[i].getType());
            };
        }
        return args;
    }

    /** Sprint 9 : enveloppe la HttpSession (créée si besoin) et pré-charge son contenu dans le cache. */
    private RSession buildSession(HttpServletRequest req) {
        RSession session = new RSession(req.getSession());
        session.loadAll();
        return session;
    }

    // fonction de test si bindObject ne va pas cycle sur lui meme pour toujours
    public Object bindObject(Class<?> type, String prefix, HttpServletRequest req) throws Exception {
        return bindObject(type, prefix, req, new HashSet<Class<?>>());
    }

    /**
     * Instancie l'objet puis appelle chaque setter avec le paramètre du même nom (préfixé si besoin).
     * Les objets imbriqués sont remplis récursivement (ami.nom). "visiting" garde les types en cours
     * pour couper une boucle infinie (Personne qui contient une Personne).
     */
    private Object bindObject(Class<?> type, String prefix, HttpServletRequest req, Set<Class<?>> visiting)
            throws Exception {
        if (!visiting.add(type)) {
            return null;
        }
        Object instance = type.getDeclaredConstructor().newInstance();
        for (Method setters : type.getDeclaredMethods()) {
            if (!setters.getName().startsWith("set"))
                continue;
            if (setters.getParameterCount() != 1)
                continue;
            String fieldName = setters.getName().substring(3);
            fieldName = Character.toLowerCase(fieldName.charAt(0))
                    + fieldName.substring(1);

            String key = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
            // String value = req.getParameter(fieldName);
            Class<?> fieldType = setters.getParameterTypes()[0];
            if (RTypeResolver.isPrimitive(fieldType)) {
                String value = req.getParameter(key);

                if (value == null)
                    continue;

                try {
                    setters.invoke(instance, convertValue(value, fieldType));
                } catch (Exception e) {
                    throw new ServletException("Erreur lors de la conversion de la valeur : " + value, e);
                }
            } else {
                if (fieldType.isArray() || List.class.isAssignableFrom(fieldType)) {
                    continue; // on aura la gestion de bindList ici.
                }
                if (!visiting.contains(fieldType)) {
                    Object nestedObj = bindObject(fieldType, key, req, new HashSet<>(visiting));
                    if (nestedObj != null) {
                        setters.invoke(instance, nestedObj);
                    }
                }
            }

        }
        visiting.remove(type); // retire le type
        return instance;
    }

    /** ?tags=a&tags=b → tableau ; pour un tableau d'objets, chaque case lit tags[i].champ. */
    private Object bindArray(Parameter param, HttpServletRequest req) throws Exception {
        String values[] = req.getParameterValues(param.getName());
        if (values == null)
            return null;
        // getComponentType() retourne le type des Objets dans l'array.
        Class<?> componentType = param.getType().getComponentType();
        Object array = Array.newInstance(componentType, values.length);
        for (int i = 0; i < values.length; i++) {
            if (RTypeResolver.isPrimitive(componentType)) {
                Array.set(array, i, convertValue(values[i], componentType));
            } else {
                String prefix = param.getName() + "[" + i + "]";
                Array.set(array, i, bindObject(componentType, prefix, req));
            }
        }
        return array;
    }

    /** Lit T dans List<T> (String par défaut si la liste n'est pas typée). */
    private Class<?> getInnerType(Type genericType) {
        if (genericType instanceof ParameterizedType pt) {
            return (Class<?>) pt.getActualTypeArguments()[0];
        }
        return String.class;
    }

    /** Même logique que bindArray, mais retourne une List (vide plutôt que null). */
    private List<Object> bindList(Parameter param, HttpServletRequest req) throws Exception {
        String values[] = req.getParameterValues(param.getName());
        if (values == null)
            return new ArrayList<>();
        Class<?> innerType = getInnerType(param.getParameterizedType());
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (RTypeResolver.isPrimitive(innerType)) {
                list.add(convertValue(value, innerType));
            } else {
                String prefix = param.getName() + "[" + i + "]";
                list.add(bindObject(innerType, prefix, req));
            }
        }
        return list;
    }

    // récupérer [K, V] dans Map<K, V>
    private Type[] getMapTypeArgs(Type genericType) {
        if (genericType instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments(); // → [String.class, String[].class]
        }
        return new Type[] { String.class, String.class };
    }

    private Object resolveMapValue(Class<?> valueType, String[] rawValues,
            String key, HttpServletRequest req) throws Exception {

        // Map<String, String[]> → valeur = tableau brut
        if (valueType == String[].class || valueType.isArray()) {
            Class<?> componentType = valueType.getComponentType();
            Object array = Array.newInstance(componentType, rawValues.length);
            for (int i = 0; i < rawValues.length; i++) {
                Array.set(array, i, convertValue(rawValues[i], componentType));
            }
            return array;
        }

        if (RTypeResolver.isPrimitive(valueType)) {
            return convertValue(rawValues[0], valueType);
        }

        // Map<String, Personne> → bindObject avec la clé comme préfixe
        // ex: key="employe" → cherche employe.nom, employe.prenom...
        return bindObject(valueType, key, req);
    }

    /**
     * Map<K, V> : tous les paramètres de la requête.
     * Si V est RMapFile, ce sont les fichiers uploadés (clé = nom du champ du formulaire).
     */
    private Object bindMap(Parameter param, HttpServletRequest req) throws Exception {
        Type[] MapTypes = getMapTypeArgs(param.getParameterizedType());
        Class<?> keyType = (Class<?>) MapTypes[0];
        Class<?> valueType = (Class<?>) MapTypes[1];

        Map<String, String[]> rawMap = req.getParameterMap();
        Map<Object, Object> result = new LinkedHashMap<>();

        if (valueType == RMapFile.class) {
            for (Part part : req.getParts()) {
                String header = part.getHeader("content-disposition");
                if (!header.contains("filename"))
                    continue;

                String fieldName = extractFilename(header, "name");
                String filename = extractFilename(header, "filename");
                String extension = extractExtension(filename);

                byte[] raw = part.getInputStream().readAllBytes();
                List<Byte> content = new ArrayList<>();
                for (byte b : raw)
                    content.add(b);

                Object key = convertValue(fieldName, keyType);
                result.put(key, new RMapFile(filename, extension, content));
            }
            return result;
        }

        for (Map.Entry<String, String[]> entry : rawMap.entrySet()) {
            Object key = convertValue(entry.getKey(), keyType);
            Object value = resolveMapValue(valueType, entry.getValue(), entry.getKey(), req);
            result.put(key, value);
        }
        return result;
    }

    /** Fichier uploadé (multipart) dont le champ porte le nom du paramètre → RMapFile, ou null. */
    private RMapFile bindFile(Parameter param, HttpServletRequest req) throws Exception {
        Part part = req.getPart(param.getName());
        if (part == null || part.getSize() == 0) {
            return null;
        }
        String header = part.getHeader("content-disposition");
        String filename = extractFilename(header, "filename");
        String extension = extractExtension(filename);

        byte[] raw = part.getInputStream().readAllBytes();
        List<Byte> content = new ArrayList<>();
        for (byte b : raw)
            content.add(b);

        return new RMapFile(filename, extension, content);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /** Lit une valeur (name ou filename) dans l'en-tête content-disposition d'un Part. */
    private String extractFilename(String header, String key) {
        if (header == null)
            return "unknown";
        for (String part : header.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(key)) {
                return trimmed.substring(trimmed.indexOf("=") + 1)
                        .replace("\"", "").trim();
            }
        }
        return "unknown";
    }

    /** Convertit la String reçue vers le type Java attendu (null si absente ou type inconnu). */
    public static Object convertValue(String value, Class<?> type) {
        if (value == null)
            return null;
        if (type == String.class)
            return value;
        if (type == int.class || type == Integer.class)
            return Integer.valueOf(value);
        if (type == long.class || type == Long.class)
            return Long.valueOf(value);
        if (type == float.class || type == Float.class)
            return Float.valueOf(value);
        if (type == double.class || type == Double.class)
            return Double.valueOf(value);
        if (type == boolean.class || type == Boolean.class)
            return Boolean.valueOf(value);
        if (type == byte.class || type == Byte.class)
            return Byte.valueOf(value);
        if (type == char.class || type == Character.class)
            return value.charAt(0);
        if (type == short.class || type == Short.class)
            return Short.valueOf(value);
        return null;
    }
}
