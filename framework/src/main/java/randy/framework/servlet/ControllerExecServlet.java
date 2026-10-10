package randy.framework.servlet;

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
import net.bytebuddy.implementation.bind.annotation.Empty;
import randy.framework.binding.BindingParameter;
import randy.framework.binding.TypeResolver;

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
