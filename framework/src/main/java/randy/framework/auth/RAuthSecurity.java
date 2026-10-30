package randy.framework.auth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import randy.framework.annotation.RAuthenticated;
import randy.framework.model.RMapping;

/**
 * Sprint 10 / 10-bis : contrôle d'accès aux routes annotées @RAuthenticated.
 * Appelé par le RFrontController AVANT d'exécuter la méthode du controller.
 *
 * Deux niveaux :
 *  - auth simple  (@RAuthenticated)                 : il faut être connecté
 *  - auth complexe (@RAuthenticated(roles = {...})) : connecté ET au moins un des rôles
 *
 * "Connecté" = l'attribut de session {@code userKey} existe.
 * Les rôles sont lus dans l'attribut de session {@code roleKey}.
 * Les deux clés et la page de login viennent des context-param du web.xml.
 */
public class RAuthSecurity {
    private final String userKey, roleKey, loginUrl;

    public RAuthSecurity(String userKey, String roleKey, String loginUrl) {
        this.userKey = userKey;
        this.roleKey = roleKey;
        this.loginUrl = loginUrl;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    /**
     * Vérifie si la requête a le droit d'accéder à la route.
     * Retourne true si oui ; sinon la réponse d'erreur est déjà envoyée (via deny)
     * et le front controller doit s'arrêter.
     */
    public boolean allow(RMapping map, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!map.isAuthRequired()) {
            return true;
        }
        // getSession(false) : ne crée pas de session vide pour un visiteur non connecté
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(userKey) == null) {
            deny(map, req, resp, 401);
            return false;
        }

        // auth complexe : liste de rôles non vide → il en faut au moins un
        List<String> roles = map.getRoles();
        if (!roles.isEmpty() && !hasAnyRole(session.getAttribute(roleKey), roles)) {
            deny(map, req, resp, 403);
            return false;
        }
        return true;
    }

    /**
     * Vrai si l'utilisateur possède au moins un des rôles demandés.
     * Le rôle en session peut être un seul objet ("ADMIN"), un tableau ou une Collection.
     */
    public boolean hasAnyRole(Object have, List<String> reqRoles) {
        if (have == null) {
            return false;
        }
        Collection<?> roles = (have instanceof Collection<?> collection) ? collection
                : (have instanceof Object[] array) ? Arrays.asList(array)
                        : List.of(have);
        for (String role : reqRoles) {
            for (Object userRole : roles) {
                if (String.valueOf(userRole).equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Envoie le refus adapté au type de route :
     *  - @RRestApi    → JSON {"error": ...} avec le code 401 ou 403
     *  - page, 401    → redirection vers loginUrl
     *  - page, 403    → erreur 403 du serveur
     */
    public void deny(RMapping map, HttpServletRequest req, HttpServletResponse resp, int status) throws IOException {
        if (map.isRestApi()) {
            resp.setStatus(status);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"error\":\"" + (status == 401 ? "non authentifié" : "accès refusé") + "\"}");
        } else if (status == 401) {
            resp.sendRedirect(req.getContextPath() + loginUrl);
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Trouve l'annotation @RAuthenticated qui s'applique à une méthode.
     * Celle de la méthode est prioritaire ; sinon on prend celle de la classe
     * (toutes les routes du controller sont alors protégées).
     * Retourne null si la route est publique.
     */
    public static RAuthenticated resolveAuth(Class<?> clazz, Method method) {
        if (method != null) {
            RAuthenticated onMethod = method.getAnnotation(RAuthenticated.class);
            if (onMethod != null) {
                return onMethod;
            }
        }
        return clazz.getAnnotation(RAuthenticated.class);
    }
}
