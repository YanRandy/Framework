package com.example.controller;

import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;

/**
 * Routes de base : page d'accueil et tests simples du routage (Sprints 1 à 4).
 * Montre les 3 types de retour possibles : String (vue), void (page debug), @RRestApi (JSON).
 */
@RController
public class RAccueilController {

    /** GET + POST par défaut (pas de "method") → vue /WEB-INF/views/accueil.jsp. */
    @RUrlMapping("/")
    public String index() {
        return "accueil";
    }

    @RUrlMapping(value = "/accueil", method = "GET")
    public String accueil() {
        return "accueil";
    }

    /** Retour void → le framework affiche sa page debug "URL supportée". */
    @RUrlMapping(value = "/test/get", method = "GET")
    public void testGet() {
        System.out.println("Résultat de la méthode GET pour /test/get");
    }

    /** Même principe, mais uniquement en POST : un GET sur cette URL donne la page 404 du framework. */
    @RUrlMapping(value = "/test/post", method = "POST")
    public void testPost() {
        System.out.println("Résultat de la méthode POST pour /test/post");
    }

    /** @RRestApi + String → renvoie une chaîne JSON ("Popo est la"), pas une vue. */
    @RRestApi
    @RUrlMapping(value = "/api/popo", method = "GET")
    public String popo() {
        return "Popo est la";
    }
}
