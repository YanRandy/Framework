package com.example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RSession;

/**
 * Sprint 9 — tests de RSession (wrapper HttpSession + cache Map).
 * Il suffit de déclarer un paramètre RSession : le framework l'injecte.
 *
 * Tout est en @RRestApi pour voir le résultat directement en JSON.
 * On utilise LinkedHashMap et pas Map.of() quand une valeur peut être null
 * (Map.of() lance une NullPointerException sur une valeur null).
 */
@RController
public class RSessionController {

    /** Contenu complet de la session + métadonnées (id, dates). */
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

    /** Persistance : 3 appels avec le même cookie → 1, 2, 3. Sans cookie → toujours 1. */
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

    /** Stocke un objet complexe (liste de Map) en session : exemple de panier. */
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

    /** Écrire dans getCache() ne touche PAS HttpSession tant qu'on n'appelle pas flush(). */
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
}
