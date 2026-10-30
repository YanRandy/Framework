package randy.framework.servlet;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import randy.framework.binding.RBindingParameter;

/**
 * Retrouve la méthode Java d'une route et l'exécute avec les arguments bindés depuis la requête.
 */
public class RControllerExec {
    private final RBindingParameter bind = new RBindingParameter();

    /**
     * Cherche la méthode par son nom uniquement (c'est ce que stocke RMapping).
     * Conséquence : pas de surcharge (deux méthodes du même nom) dans un controller.
     */
    public Method findMethod(Class<?> clazz, String methodName) throws ServletException {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName))
                return m;
        }
        throw new ServletException("Méthode introuvable : " + methodName);
    }

    /** Construit les arguments via RBindingParameter puis appelle la méthode sur l'instance. */
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
