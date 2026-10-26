package com.example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randy.framework.annotation.RAuthenticated;
import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RSession;

/**
 * Sprint 9  — tests de la session (wrapper HttpSession + cache Map)
 * Sprint 10 — tests de l'authentification et des rôles (@RAuthenticated)
 *
 * Tout est en @RRestApi pour voir le résultat directement en JSON.
 * On utilise LinkedHashMap et pas Map.of() quand une valeur peut être null
 * (Map.of() lance une NullPointerException sur une valeur null).
 */
@RController
public class RSprintController {

    // ══════════════════════════════════════════════════════════════════
    //  SPRINT 9 — SESSION
    // ══════════════════════════════════════════════════════════════════

    @RRestApi
    @RUrlMapping(value = "/sprint/session/info", method = "GET")
    public Object info(RSession session) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("sessionId", session.getId());
        r.put("isNew", session.isNew());
        r.put("creationTime", session.getCreationTime());
        r.put("lastAccessed", session.getLastAccessedTime());
        r.put("contenu", session.getAll());
        return r;
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/session/set", method = "POST")
    public Object set(String cle, String valeur, RSession session) {
        if (cle == null || valeur == null) {
            return Map.of("error", "paramètres 'cle' et 'valeur' requis");
        }
        session.set(cle, valeur);
        return Map.of("message", "enregistré", "cle", cle, "valeur", valeur);
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/session/get", method = "GET")
    public Object get(String cle, RSession session) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("cle", cle);
        r.put("existe", cle != null && session.has(cle));
        r.put("valeur", cle != null ? session.get(cle) : null);
        return r;
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/session/remove", method = "POST")
    public Object remove(String cle, RSession session) {
        boolean existait = cle != null && session.has(cle);
        if (cle != null) session.remove(cle);
        return Map.of("cle", String.valueOf(cle), "existait", existait);
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/session/clear", method = "POST")
    public Object clear(RSession session) {
        int avant = session.getAll().size();
        session.clear();
        return Map.of("attributsSupprimes", avant, "restant", session.getAll().size());
    }

    // Persistance : 3 appels avec le même cookie → 1, 2, 3. Sans cookie → toujours 1.
    @RRestApi
    @RUrlMapping(value = "/sprint/session/compteur", method = "GET")
    public Object compteur(RSession session) {
        Integer n = session.get("compteur", Integer.class);
        n = (n == null) ? 1 : n + 1;
        session.set("compteur", n);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("compteur", n);
        r.put("sessionId", session.getId());
        return r;
    }

    @SuppressWarnings("unchecked")
    @RRestApi
    @RUrlMapping(value = "/sprint/panier/ajouter", method = "POST")
    public Object ajouterAuPanier(String produit, Integer quantite, RSession session) {
        if (produit == null || quantite == null) {
            return Map.of("error", "paramètres 'produit' et 'quantite' requis");
        }
        List<Map<String, Object>> panier = session.get("panier", List.class);
        if (panier == null) panier = new ArrayList<>();

        Map<String, Object> item = new HashMap<>();
        item.put("produit", produit);
        item.put("quantite", quantite);
        panier.add(item);

        session.set("panier", panier);
        return Map.of("message", "ajouté", "total", panier.size());
    }

    @SuppressWarnings("unchecked")
    @RRestApi
    @RUrlMapping(value = "/sprint/panier", method = "GET")
    public Object voirPanier(RSession session) {
        List<Map<String, Object>> panier = session.get("panier", List.class);
        if (panier == null) panier = new ArrayList<>();
        return Map.of("panier", panier, "total", panier.size());
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/panier/vider", method = "POST")
    public Object viderPanier(RSession session) {
        session.remove("panier");
        return Map.of("message", "panier vidé");
    }

    // Écrire dans getCache() ne touche PAS HttpSession tant qu'on n'appelle pas flush().
    @RRestApi
    @RUrlMapping(value = "/sprint/session/cache", method = "GET")
    public Object testCache(RSession session) {
        session.getCache().put("viaCache", "valeur");
        boolean avant = session.raw().getAttribute("viaCache") != null; // false
        session.flush();
        boolean apres = session.raw().getAttribute("viaCache") != null; // true

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("dansTomcatAvantFlush", avant);
        r.put("dansTomcatApresFlush", apres);
        return r;
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/session/timeout", method = "POST")
    public Object timeout(Integer secondes, RSession session) {
        if (secondes == null) return Map.of("error", "paramètre 'secondes' requis");
        session.setMaxInactiveInterval(secondes);
        return Map.of("timeoutSecondes", secondes);
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/session/invalidate", method = "POST")
    public Object invalidate(RSession session) {
        String ancienId = session.getId();
        session.invalidate();
        return Map.of("message", "session détruite", "ancienId", ancienId);
    }

    // ══════════════════════════════════════════════════════════════════
    //  SPRINT 10 / 10-bis — AUTHENTIFICATION ET RÔLES
    //
    //  Les clés de session "user" et "role" doivent être les mêmes que les
    //  context-param authUserKey / authRoleKey du web.xml.
    //  Mot de passe codé en dur (1234) : pour les tests uniquement.
    // ══════════════════════════════════════════════════════════════════

    // Cible de la redirection des pages non connectées (context-param loginUrl = /login)
    @RRestApi
    @RUrlMapping(value = "/login", method = "GET")
    public Object loginPage() {
        return Map.of("message",
                "Non connecté. POST /sprint/auth/login avec username, password (1234) et role.");
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/auth/login", method = "POST")
    public Object login(String username, String password, String role, RSession session) {
        if (username == null || !"1234".equals(password)) {
            return Map.of("error", "identifiants invalides (test : mot de passe = 1234)");
        }
        String r = (role == null || role.isBlank()) ? "USER" : role;
        session.set("user", username);
        session.set("role", r);
        return Map.of("message", "connecté", "user", username, "role", r);
    }

    @RRestApi
    @RUrlMapping(value = "/sprint/auth/logout", method = "POST")
    public Object logout(RSession session) {
        session.invalidate();
        return Map.of("message", "déconnecté");
    }

    // PUBLIC : aucune annotation → toujours 200
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/public", method = "GET")
    public Object publique() {
        return Map.of("message", "route ouverte à tous");
    }

    // PUBLIC : pour voir ce que contient la session (debug)
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/whoami", method = "GET")
    public Object whoami(RSession session) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("user", session.get("user"));
        r.put("role", session.get("role"));
        r.put("sessionId", session.getId());
        return r;
    }

    // Sprint 10 : connecté suffit → 401 sans login, 200 avec
    @RAuthenticated
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/profil", method = "GET")
    public Object profil(RSession session) {
        return Map.of("message", "profil de " + session.get("user"));
    }

    // Sprint 10-bis : rôle ADMIN requis → 401 sans login, 403 si autre rôle, 200 si ADMIN
    @RAuthenticated(roles = { "ADMIN" })
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/admin", method = "GET")
    public Object admin(RSession session) {
        return Map.of("message", "zone admin", "user", String.valueOf(session.get("user")));
    }

    // Sprint 10-bis : au moins un des rôles → ADMIN ou MANAGER passent, USER non
    @RAuthenticated(roles = { "ADMIN", "MANAGER" })
    @RRestApi
    @RUrlMapping(value = "/sprint/auth/staff", method = "GET")
    public Object staff(RSession session) {
        return Map.of("message", "zone staff", "role", String.valueOf(session.get("role")));
    }

    // Route NON REST protégée : sans login → redirection 302 vers /login (pas de JSON)
    @RAuthenticated
    @RUrlMapping(value = "/sprint/auth/page", method = "GET")
    public String pageProtegee() {
        return "accueil"; // /WEB-INF/views/accueil.jsp
    }
}