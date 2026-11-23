package randy.framework.interceptor;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import randy.framework.model.RMapping;

/**
 * Comportement associé à une annotation (via @RHandledBy), exécuté à chaque requête.
 * Une seule instance par classe, partagée par toutes les requêtes :
 * aucun état par requête dans les champs, seulement la config lue dans init().
 */
public interface RInterceptor {

    /** Appelée une seule fois au démarrage : lire ici les context-params du web.xml. */
    default void init(ServletContext ctx) {}

    /**
     * Avant l'exécution du controller. false = on s'arrête là,
     * et l'intercepteur a déjà écrit la réponse (redirection, 401, 403...).
     */
    default boolean before(RMapping map, HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        return true;
    }
}
