package randy.framework.binding;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

public class BindingParameter {

    public Object[] bindAll(Parameter[] params, ApplicationContext ctx, HttpServletRequest req) throws Exception {
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            args[i] = switch (TypeResolver.resolve(params[i])) {
                case APPLICATION_CONTEXT -> ctx;
                case PRIMITIVE -> convertValue(
                        req.getParameter(params[i].getName()),
                        params[i].getType());
                case ARRAY -> bindArray(params[i], req);
                case LIST -> bindList(params[i], req);
                case MAP -> bindMap(params[i], req);
                case OBJECT -> bindObject(
                        params[i].getType(),
                        params[i].getName(),
                        req);
                default -> throw new IllegalArgumentException(
                        "Type non supporté : " + params[i].getType());
            };
        }
        return args;
    }

    // fonction de test si bindObject ne va pas cycle sur lui meme pour toujours
    public Object bindObject(Class<?> type, String prefix, HttpServletRequest req) throws Exception {
        return bindObject(type, prefix, req, new HashSet<Class<?>>());
    }

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
            if (TypeResolver.isPrimitive(fieldType)) {
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
                    Object nestedObj = bindObject(fieldType, key, req, visiting);
                    if (nestedObj != null) {
                        setters.invoke(instance, nestedObj);
                    }
                }
            }

        }
        visiting.remove(type); // retire le type
        return instance;
    }

    private Object bindArray(Parameter param, HttpServletRequest req) throws Exception {
        String values[] = req.getParameterValues(param.getName());
        if (values == null)
            return null;
        // getComponentType() retourne le type des Objets dans l'array.
        Class<?> componentType = param.getType().getComponentType();
        Object array = Array.newInstance(componentType, values.length);
        for (int i = 0; i < values.length; i++) {
            if (TypeResolver.isPrimitive(componentType)) {
                Array.set(array, i, convertValue(values[i], componentType));
            } else {
                String prefix = param.getName() + "[" + i + "]";
                Array.set(array, i, bindObject(componentType, prefix, req));
            }
        }
        return array;
    }

    private Class<?> getInnerType(Type genericType) {
        if (genericType instanceof ParameterizedType pt) {
            return (Class<?>) pt.getActualTypeArguments()[0];
            // List<String> → String.class
            // List<Personne> → Personne.class
        }
        return String.class; // fallback si pas de générique
    }

    private List<Object> bindList(Parameter param, HttpServletRequest req) throws Exception {
        String values[] = req.getParameterValues(param.getName());
        if (values == null)
            return new ArrayList<>();
        Class<?> innerType = getInnerType(param.getParameterizedType());
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (TypeResolver.isPrimitive(innerType)) {
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
        return new Type[] { String.class, String.class }; // fallback
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

        // Map<String, String> → première valeur seulement
        if (TypeResolver.isPrimitive(valueType)) {
            return convertValue(rawValues[0], valueType);
        }

        // Map<String, Personne> → bindObject avec la clé comme préfixe
        // ex: key="employe" → cherche employe.nom, employe.prenom...
        return bindObject(valueType, key, req);
    }

    private Object bindMap(Parameter param, HttpServletRequest req) throws Exception {
    // fonction de test si bindObject ne va pas cycle sur lui meme pour toujours
        Type[] MapTypes = getMapTypeArgs(param.getParameterizedType());
        Class<?> keyType = (Class<?>) MapTypes[0];
        Class<?> valueType = (Class<?>) MapTypes[1];

        // classic map of http
        Map<String, String[]> rawMap = req.getParameterMap();
        Map<Object, Object> result = new HashMap<>();

        for (Map.Entry<String, String[]> entry : rawMap.entrySet()) {
            Object key = convertValue(entry.getKey(), keyType);
            Object value = resolveMapValue(valueType, entry.getValue(), entry.getKey(), req);
            result.put(key, value);
        }
        return result;
    }

    private static Object convertValue(String value, Class<?> params) {
        if (value == null)
            return null;
        if (params == String.class)
            return value;
        if (params == int.class || params == Integer.class)
            return Integer.valueOf(value);
        if (params == long.class || params == Long.class)
            return Long.valueOf(value);
        if (params == float.class || params == Float.class)
            return Float.valueOf(value);
        if (params == double.class || params == Double.class)
            return Double.valueOf(value);
        if (params == boolean.class || params == Boolean.class)
            return Boolean.valueOf(value);
        return null;
    }
}
