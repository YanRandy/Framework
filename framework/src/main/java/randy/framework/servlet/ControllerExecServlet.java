package randy.framework.servlet;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.ServletException;

public class ControllerExecServlet {
    public Method findMethod(Class<?> clazz, String methodName) throws ServletException {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName))
                return m;
        }
        throw new ServletException("Méthode introuvable : " + methodName);
    }

    public Object invoke(Method method, Object instance, ApplicationContext ctx)
            throws Exception {
        Class<?>[] params = method.getParameterTypes();
        if (params.length == 1 && ApplicationContext.class.isAssignableFrom(params[0])) {
            return method.invoke(instance, ctx);
        }
        return method.invoke(instance);
    }
}
