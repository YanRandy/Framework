package com.example.controller;

import org.springframework.context.ApplicationContext;

import com.example.service.RLivreService;

import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RModelAndView;

/**
 * Livres : même donnée rendue en JSP (RModelAndView) ou en JSON (@RRestApi).
 * Le service est récupéré dans Spring grâce au paramètre ApplicationContext (Sprint 6).
 */
@RController
public class RLivreController {

    /** Vue /WEB-INF/views/livres.jsp, la liste est disponible en ${livres}. */
    @RUrlMapping(value = "/livres", method = "GET")
    public RModelAndView afficherLivres(ApplicationContext ctx) {
        RLivreService service = ctx.getBean(RLivreService.class);
        RModelAndView mv = new RModelAndView("livres");
        mv.setAttribute("livres", service.getTousLesLivres().toArray());
        return mv;
    }

    @RRestApi
    @RUrlMapping(value = "/api/livres", method = "GET")
    public Object getLivres(ApplicationContext ctx) {
        return ctx.getBean(RLivreService.class).getTousLesLivres();
    }
}
