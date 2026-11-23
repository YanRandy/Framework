package randy.framework.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Erreurs HTTP renvoyées par le framework (page d'erreur standard du serveur) :
 * - notFound : route inconnue → 404
 * - error    : exception dans le controller → 500 + message de l'exception
 */
public class RHttpError {

    public static void notFound(String pathInfo, String httpMethod, HttpServletResponse response)
            throws IOException {
        response.sendError(404, "Aucune route pour " + httpMethod + " " + pathInfo);
    }

    public static void error(Exception e, HttpServletResponse response) throws IOException {
        // method.invoke() emballe l'exception du controller : on récupère la vraie
        Throwable cause = (e instanceof InvocationTargetException ite && ite.getCause() != null)
                ? ite.getCause()
                : e;
        cause.printStackTrace(); // stack trace complète dans les logs du serveur
        response.sendError(500, cause.getClass().getSimpleName() + " : " + cause.getMessage());
    }
}
