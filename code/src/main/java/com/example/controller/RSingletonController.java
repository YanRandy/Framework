package com.example.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RSession;

/**
 * Test du controller singleton (RControllerRegistry) : une seule instance pour
 * toute l'application, réutilisée à chaque requête et par tous les utilisateurs.
 *
 * Deux choses à vérifier :
 * - /sprint/singleton/instance : "instancesCreees" reste à 1 et "instanceId" ne change pas.
 * - /sprint/singleton/ajouter puis /lire avec deux cookies différents : le champ
 * dernierProduit est partagé entre les utilisateurs, la session ne l'est pas.
 */
@RController
public class RSingletonController {

    /** static : compte les constructions, même si plusieurs instances existaient. */
    private static final AtomicInteger INSTANCES_CREEES = new AtomicInteger();

    /** Champ d'instance : garde sa valeur d'une requête à l'autre grâce au singleton. */
    private final AtomicInteger appels = new AtomicInteger();

    /** À NE PAS faire en vrai : partagé entre TOUS les utilisateurs. */
    private volatile String dernierProduit;

    public RSingletonController() {
        INSTANCES_CREEES.incrementAndGet();
    }

    /** instancesCreees = 1 et instanceId fixe → singleton. appels augmente à chaque requête. */
    @RRestApi
    @RUrlMapping(value = "/sprint/singleton/instance", method = "GET")
    public Object instance() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("instancesCreees", INSTANCES_CREEES.get());
        r.put("instanceId", System.identityHashCode(this));
        r.put("appels", appels.incrementAndGet());
        return r;
    }

    /** Écrit le produit dans le champ (partagé) ET dans la session (propre à l'utilisateur). */
    @RRestApi
    @RUrlMapping(value = "/sprint/singleton/ajouter", method = "POST")
    public Object ajouter(String produit, RSession session) {
        if (produit == null) {
            return Map.of("error", "paramètre 'produit' requis");
        }
        dernierProduit = produit;
        session.set("dernier", produit);
        return Map.of("message", "ajouté", "produit", produit);
    }

    /** Un autre utilisateur voit le champ de A, mais sa session à lui reste vide. */
    @RRestApi
    @RUrlMapping(value = "/sprint/singleton/lire", method = "GET")
    public Object lire(RSession session) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("champPartage", dernierProduit);
        r.put("session", session.get("dernier"));
        return r;
    }
}
