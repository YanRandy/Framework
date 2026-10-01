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

        // In ControllerInvoker — handle mixed parameters
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            if (ApplicationContext.class.isAssignableFrom(params[i].getType())) {
                args[i] = ctx; // ← inject Spring container
            } else {
                String val = req.getParameter(params[i].getName());
                args[i] = convertValue(val, params[i].getType());
            }
        }
        return method.invoke(instance, args);
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
