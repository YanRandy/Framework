package randy.framework.servlet;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import randy.framework.binding.BindingParameter;

public class ControllerExecServlet {
    private final BindingParameter bind = new BindingParameter();

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
        if(params.length == 0) {
            return method.invoke(instance);
        }
        Object[] args = bind.bindAll(params, ctx, req);
        return method.invoke(instance, args);
    }
}
