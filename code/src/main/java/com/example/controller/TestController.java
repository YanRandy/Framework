package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.example.entity.Livre;
import com.example.entity.Personne;
import com.example.service.LivreService;
import com.example.service.PersonneService;

import randy.framework.annotation.Controller;
import randy.framework.annotation.UrlMapping;
import randy.framework.annotation.RestApi;
import randy.framework.model.ModelAndView;

@Controller
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

    @UrlMapping(value = "/test1", method = "POST")
    public String soumettreFormulaire() {
        System.out.println(" -> [CONTROLLER] Exécution de la méthode POST pour /test");
        return "/test POST";
    }

    @UrlMapping(value = "/test1", method = "GET")
    public ModelAndView afficherMessages(ApplicationContext ctx) {
        LivreService service = ctx.getBean(LivreService.class);
        List<Livre> livres = service.getTousLesLivres();

        ModelAndView mv = new ModelAndView();
        mv.setView("test1");
        mv.setAttribute("livres", livres.toArray());
        return mv;
    }

    @UrlMapping(value = "/accueil", method = "GET")
    public String accueil() {
        return "accueil"; // → cherche /WEB-INF/views/accueil.jsp (ou .html)
    }

    @UrlMapping(value = "/livres", method = "GET")
    public ModelAndView afficherLivres(ApplicationContext ctx) {
        LivreService service = ctx.getBean(LivreService.class);
        ModelAndView mv = new ModelAndView();
        mv.setView("test");
        mv.setAttribute("livres", service.getTousLesLivres().toArray());
        return mv;
    }

    @RestApi
    @UrlMapping(value = "/api/livres", method = "GET")
    public Object getLivres(ApplicationContext ctx) {
        LivreService service = ctx.getBean(LivreService.class);
        return service.getTousLesLivres();
    }

    @RestApi
    @UrlMapping (value = "/api/popo", method = "GET")
    public String Popo() {
        return "Popo est la";
    }

    @UrlMapping(value = "/formulaire", method = "GET")
    public ModelAndView afficherFormulaire() {
        ModelAndView mv = new ModelAndView();
        mv.setView("formulaire");
        return mv;
    }

    @UrlMapping(value = "/personnes", method = "GET")
    public ModelAndView afficherPersonne(ApplicationContext ctx) {
        PersonneService service = ctx.getBean(PersonneService.class);
        ModelAndView mv = new ModelAndView();
        mv.setView("personnes");
        mv.setAttribute("personnes", service.getToutesLesPersonnes().toArray());
        return mv;
    }

    @RestApi
    @UrlMapping(value = "/api/personnes", method = "GET")
    public List<Personne> getPersonnesApi(ApplicationContext ctx) {
        PersonneService service = ctx.getBean(PersonneService.class);
        return service.getToutesLesPersonnes();
    }

    // Insert + return JSON confirmation
    @RestApi
    @UrlMapping(value = "/api/save", method = "POST")
    public Object saveApi(String nom, String prenom, int age, ApplicationContext ctx) {
        PersonneService service = ctx.getBean(PersonneService.class);
        service.save(nom, prenom, age);   // ← insert still happens
        return Map.of("status", "ok", "nom", nom);  // ← but response is JSON not a view
    }

    // Insert + redirect to JSP view
    @UrlMapping(value = "/save", method = "POST")
    public String save(String nom, String prenom, int age, ApplicationContext ctx) {
        PersonneService service = ctx.getBean(PersonneService.class);
        service.save(nom, prenom, age);   // ← same insert
        return "confirmation";            // ← but response is a JSP page
    }

    @UrlMapping(value = "/complexe/save", method = "POST")
    public String save(Personne personne, ApplicationContext ctx) {
        ctx.getBean(PersonneService.class).save(personne);
        return "confirmation";
    }

    @RestApi
    @UrlMapping(value = "/api/complexe/save", method = "GET")
    public Personne saveApi(Personne e, ApplicationContext ctx) {
        ctx.getBean(PersonneService.class).save(e);
        return e;
    }
}
