package randy.framework.servlet;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.implementation.bind.annotation.Empty;
import randy.framework.binding.TypeResolver;

public class ControllerExecServlet {
    public Method findMethod(Class<?> clazz, String methodName) throws ServletException {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName))
                return m;
        }
        throw new ServletException("Méthode introuvable : " + methodName);
    }

    public Object invoke(Method method, Object instance, ApplicationContext ctx, HttpServletRequest req)
            throws Exception {
        Parameter[] params = method.getParameters();
        if (params.length == 0) {
            return method.invoke(instance);
        }
        if (params.length == 1 && ApplicationContext.class.isAssignableFrom(params[0].getType())) {
            return method.invoke(instance, ctx);
        }

        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            // Class<?> type = params[i].getType();
            // if (ApplicationContext.class.isAssignableFrom(params[i].getType())) {
            // args[i] = ctx;
            // } else if (TypeResolver.isPrimitive(type)) {
            // String val = req.getParameter(params[i].getName());
            // args[i] = convertValue(val, params[i].getType());
            // } else {
            // args[i] = bindObject(type, params[i].getName(), req);
            // }
            args[i] = switch (TypeResolver.resolve(params[i])) {
                case APPLICATION_CONTEXT -> ctx;
                case PRIMITIVE -> convertValue(
                        req.getParameter(params[i].getName()),
                        params[i].getType());
                case ARRAY -> bindArray(params[i], req); // ← ici
                case LIST -> bindList(params[i], req);
                case OBJECT -> bindObject(
                        params[i].getType(),
                        params[i].getName(),
                        req);
                default -> throw new IllegalArgumentException("Unexpected value: " + TypeResolver.resolve(params[i]));
            };
        }
        return method.invoke(instance, args);
    }

    // private boolean isPrimitive(Class<?> type) {
    // return type.isPrimitive() || type == String.class || type == Integer.class ||
    // type == Long.class
    // || type == Double.class || type == Boolean.class;
    // }

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
        for (int i=0; i<values.length; i++) {
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
