package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.example.entity.RPersonne;
import com.example.service.RPersonneService;

import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RModelAndView;

/**
 * CRUD simple des personnes : formulaire, liste et insertion.
 * Chaque action existe en version vue (JSP) et en version API (JSON).
 *
 * Attention : le framework retrouve la méthode par son NOM seulement,
 * donc pas de surcharge (deux méthodes "save") dans un même controller.
 */
@RController
public class RPersonneController {

    /** Formulaire d'ajout (vue formulaire.jsp, qui poste vers /save). */
    @RUrlMapping(value = "/formulaire", method = "GET")
    public RModelAndView afficherFormulaire() {
        return new RModelAndView("formulaire");
    }

    /** Liste des personnes (vue personnes.jsp). */
    @RUrlMapping(value = "/personnes", method = "GET")
    public RModelAndView afficherPersonnes(ApplicationContext ctx) {
        RPersonneService service = ctx.getBean(RPersonneService.class);
        RModelAndView mv = new RModelAndView("personnes");
        mv.setAttribute("personnes", service.getToutesLesPersonnes().toArray());
        return mv;
    }

    @RRestApi
    @RUrlMapping(value = "/api/personnes", method = "GET")
    public List<RPersonne> getPersonnesApi(ApplicationContext ctx) {
        return ctx.getBean(RPersonneService.class).getToutesLesPersonnes();
    }

    /** Insertion par paramètres primitifs (nom, prenom, age) → page de confirmation. */
    @RUrlMapping(value = "/save", method = "POST")
    public String save(String nom, String prenom, int age, ApplicationContext ctx) {
        ctx.getBean(RPersonneService.class).save(nom, prenom, age);
        return "confirmation";
    }

    /** Même insertion, mais la réponse est du JSON. */
    @RRestApi
    @RUrlMapping(value = "/api/save", method = "POST")
    public Object saveApi(String nom, String prenom, int age, ApplicationContext ctx) {
        ctx.getBean(RPersonneService.class).save(nom, prenom, age);
        return Map.of("status", "ok", "nom", nom);
    }

    /** Insertion par binding objet : nom, prenom, age remplissent directement un RPersonne. */
    @RUrlMapping(value = "/complexe/save", method = "POST")
    public String saveComplexe(RPersonne personne, ApplicationContext ctx) {
        ctx.getBean(RPersonneService.class).save(personne);
        return "confirmation";
    }

    @RRestApi
    @RUrlMapping(value = "/api/complexe/save", method = "GET")
    public RPersonne saveComplexeApi(RPersonne personne, ApplicationContext ctx) {
        ctx.getBean(RPersonneService.class).save(personne);
        return personne;
    }
}
