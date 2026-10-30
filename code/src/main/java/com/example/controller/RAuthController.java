package com.example.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import randy.framework.annotation.RAuthenticated;
import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RSession;

/**
 * Sprint 10 / 10-bis — authentification et rôles avec @RAuthenticated sur les
 * MÉTHODES.
 *
 * Pas d'annotation → public
 * 
 * @RAuthenticated → auth simple : il faut être connecté
 * @RAuthenticated(roles = {"ADMIN"}) → auth complexe : connecté ET un des rôles
 *
 *                       Les clés de session "user" et "role" doivent être les
 *                       mêmes que les
 *                       context-param authUserKey / authRoleKey du web.xml.
 *                       Mot de passe codé en dur (1234) : pour les tests
 *                       uniquement. (lol)
 */
@RController
public class RAuthController {

    /**
     * Cible de la redirection des pages non connectées (context-param loginUrl =
     * /login) : affiche directement le formulaire de login (vue test-auth.jsp).
     */
    @RUrlMapping(value = "/login", method = "GET")
    public String loginPage() {
        return "test-auth";
    }

    /**
     * Page de test : formulaires login / logout + liens vers les routes protégées
     * (vue test-auth.jsp).
     */
    @RUrlMapping(value = "/sprint/auth/test", method = "GET")
    public String testPage() {
        return "test-auth";
    }

    /**
     * "Connexion" = poser user et role dans la session. C'est tout ce que le
     * framework vérifie.
     */
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/login", method = "POST")
    public Object login(String username, String password, String role, RSession session) {
        if (username == null || (password == null) || password.isEmpty()) {
            return Map.of("error", "identifiants invalides (username et password requis)");
        }
        // String r = (role == null || role.isBlank()) ? "" : role;
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("message", "connecté");
        r.put("user", username);

        if (role == null || role.isBlank()) {
            session.remove("role"); // pas de rôle, et on efface un éventuel ancien rôle
            r.put("role", null);
        } else {
            session.set("role", role);
            r.put("role", role);
        }
        session.set("user", username);
        // session.set("role", r);
        // return Map.of("message", "connecté", "user", username, "role", r);
        // r.put("sessionId", session.getId());
        // r.put("userDansTomcat", session.raw().getAttribute("user"));
        return r;
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/auth/logout", method = "POST")
    public Object logout(RSession session) {
        session.invalidate();
        return Map.of("message", "déconnecté");
    }

    /** PUBLIC : aucune annotation → toujours 200. */
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/public", method = "GET")
    public Object publique() {
        return Map.of("message", "route ouverte à tous");
    }

    /** PUBLIC : pour voir ce que contient la session (debug). */
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/whoami", method = "GET")
    public Object whoami(RSession session) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("user", session.get("user"));
        r.put("role", session.get("role"));
        r.put("sessionId", session.getId());
        return r;
    }

    /**
     * Sprint 10-bis : connecté ET un rôle parmi USER, ADMIN, MANAGER → 401 sans
     * login, 403 si connecté sans rôle, 200 sinon.
     */
    @RAuthenticated(roles = {"USER", "ADMIN", "MANAGER"})
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/profil", method = "GET")
    public Object profil(RSession session) {
        return Map.of("message", "profil de " + session.get("user"));
    }

    /**
     * Sprint 10-bis (auth complexe) : rôle ADMIN requis → 401 sans login, 403 si
     * autre rôle, 200 si ADMIN.
     */
    @RAuthenticated(roles = { "ADMIN" })
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/admin", method = "GET")
    public Object admin(RSession session) {
        return Map.of("message", "zone admin", "user", String.valueOf(session.get("user")));
    }

    /**
     * Sprint 10-bis : AU MOINS UN des rôles suffit → ADMIN ou MANAGER passent, USER
     * non.
     */
    @RAuthenticated(roles = { "ADMIN", "MANAGER" })
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/staff", method = "GET")
    public Object staff(RSession session) {
        return Map.of("message", "zone staff", "role", String.valueOf(session.get("role")));
    }

    /**
     * Route NON REST protégée (auth simple, sans rôle) : sans login → redirection
     * 302 vers /login ; connecté, même sans rôle → vue page-protegee.jsp.
     */
    @RAuthenticated
    @RUrlMapping(value = "/sprint/auth/page", method = "GET")
    public String pageProtegee() {
        return "page-protegee";
    }
}
