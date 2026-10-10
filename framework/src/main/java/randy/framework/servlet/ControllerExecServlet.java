package randy.framework.servlet;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
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
            Class<?> type = params[i].getType();
            if (ApplicationContext.class.isAssignableFrom(params[i].getType())) {
                args[i] = ctx;
            } else if (TypeResolver.isPrimitive(type)) {
                String val = req.getParameter(params[i].getName());
                args[i] = convertValue(val, params[i].getType());
            } else {
                args[i] = bindObject(type, params[i].getName(), req);
            }
        }
        return method.invoke(instance, args);
    }

    // private boolean isPrimitive(Class<?> type) {
    //     return type.isPrimitive() || type == String.class || type == Integer.class || type == Long.class
    //             || type == Double.class || type == Boolean.class;
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
            }
            else {
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
