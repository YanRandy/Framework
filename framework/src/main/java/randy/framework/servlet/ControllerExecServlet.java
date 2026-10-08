package randy.framework.servlet;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

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
            } else if (isPrimitive(type)) {
                String val = req.getParameter(params[i].getName());
                args[i] = convertValue(val, params[i].getType());
            } else {
                args[i] = bindObject(type, req);
            }
        }
        return method.invoke(instance, args);
    }

    private boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || type == String.class || type == Integer.class || type == Long.class
                || type == Double.class || type == Boolean.class;
    }

    private Object bindObject(Class<?> type, HttpServletRequest req) throws Exception {
        Object instance = type.getDeclaredConstructor().newInstance();
        for (Method setters : type.getDeclaredMethods()) {
            if (!setters.getName().startsWith("set"))
                continue;
            if (setters.getParameterCount() != 1)
                continue;
            String fieldName = setters.getName().substring(3);
            fieldName = Character.toLowerCase(fieldName.charAt(0))
                    + fieldName.substring(1);
            String value = req.getParameter(fieldName);
            if (value == null)
                continue;

            try {
                setters.invoke(instance, convertValue(value, setters.getParameterTypes()[0]));
            } catch (Exception e) {
                throw new ServletException("Erreur lors de la conversion de la valeur : " + value, e);
            }
        }
        return instance;
    }

    public Object convertValue(String value, Class<?> params) {
        if (value == null)
            return null;
        if (params == String.class)
            return value;
        if (params == int.class || params == Integer.class)
            return Integer.valueOf(value);
        if (params == long.class || params == Long.class)
            return Long.valueOf(value);
        if (params == double.class || params == Double.class)
            return Double.valueOf(value);
        if (params == boolean.class || params == Boolean.class)
            return Boolean.valueOf(value);
        return null;
    }
}
