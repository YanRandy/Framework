package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.example.entity.Livre;
import com.example.entity.Personne;
import com.example.service.LivreService;
import com.example.service.PersonneService;

import randy.framework.annotation.RController;
import randy.framework.annotation.RUrlMapping;
import randy.framework.annotation.RRestApi;
import randy.framework.model.RModelAndView;

@RController
public class TestController {

    // @UrlMapping(value = "/test", method = "GET")
    // public ModelAndView afficherFormulaire() {
    // ModelAndView mv = new ModelAndView();
    // mv.setView("test");
    // String[] listMessage = { "Bonjour", "Salut", "Hello", "Hi" };
    // mv.setAttribute("message", "Bonjour");
    // mv.setAttribute("message", listMessage);
    // return mv;
    // }

    @RUrlMapping(value = "/test1", method = "POST")
    public String soumettreFormulaire() {
        System.out.println(" -> [CONTROLLER] Exécution de la méthode POST pour /test");
        return "/test POST";
    }

    @RUrlMapping(value = "/test1", method = "GET")
    public RModelAndView afficherMessages(ApplicationContext ctx) {
        LivreService service = ctx.getBean(LivreService.class);
        List<Livre> livres = service.getTousLesLivres();

        RModelAndView mv = new RModelAndView();
        mv.setView("test1");
        mv.setAttribute("livres", livres.toArray());
        return mv;
    }

    @RUrlMapping(value = "/accueil", method = "GET")
    public String accueil() {
        return "accueil"; // → cherche /WEB-INF/views/accueil.jsp (ou .html)
    }

    @RUrlMapping(value = "/livres", method = "GET")
    public RModelAndView afficherLivres(ApplicationContext ctx) {
        LivreService service = ctx.getBean(LivreService.class);
        RModelAndView mv = new RModelAndView();
        mv.setView("test");
        mv.setAttribute("livres", service.getTousLesLivres().toArray());
        return mv;
    }

    @RRestApi
    @RUrlMapping(value = "/api/livres", method = "GET")
    public Object getLivres(ApplicationContext ctx) {
        LivreService service = ctx.getBean(LivreService.class);
        return service.getTousLesLivres();
    }

    @RRestApi
    @RUrlMapping (value = "/api/popo", method = "GET")
    public String Popo() {
        return "Popo est la";
    }

    @RUrlMapping(value = "/formulaire", method = "GET")
    public RModelAndView afficherFormulaire() {
        RModelAndView mv = new RModelAndView();
        mv.setView("formulaire");
        return mv;
    }

    @RUrlMapping(value = "/personnes", method = "GET")
    public RModelAndView afficherPersonne(ApplicationContext ctx) {
        PersonneService service = ctx.getBean(PersonneService.class);
        RModelAndView mv = new RModelAndView();
        mv.setView("personnes");
        mv.setAttribute("personnes", service.getToutesLesPersonnes().toArray());
        return mv;
    }

    @RRestApi
    @RUrlMapping(value = "/api/personnes", method = "GET")
    public List<Personne> getPersonnesApi(ApplicationContext ctx) {
        PersonneService service = ctx.getBean(PersonneService.class);
        return service.getToutesLesPersonnes();
    }

    // Insert + return JSON confirmation
    @RRestApi
    @RUrlMapping(value = "/api/save", method = "POST")
    public Object saveApi(String nom, String prenom, int age, ApplicationContext ctx) {
        PersonneService service = ctx.getBean(PersonneService.class);
        service.save(nom, prenom, age);   // ← insert still happens
        return Map.of("status", "ok", "nom", nom);  // ← but response is JSON not a view
    }

    // Insert + redirect to JSP view
    @RUrlMapping(value = "/save", method = "POST")
    public String save(String nom, String prenom, int age, ApplicationContext ctx) {
        PersonneService service = ctx.getBean(PersonneService.class);
        service.save(nom, prenom, age);   // ← same insert
        return "confirmation";            // ← but response is a JSP page
    }

    @RUrlMapping(value = "/complexe/save", method = "POST")
    public String save(Personne personne, ApplicationContext ctx) {
        ctx.getBean(PersonneService.class).save(personne);
        return "confirmation";
    }

    @RRestApi
    @RUrlMapping(value = "/api/complexe/save", method = "GET")
    public Personne saveApi(Personne e, ApplicationContext ctx) {
        ctx.getBean(PersonneService.class).save(e);
        return e;
    }
}
