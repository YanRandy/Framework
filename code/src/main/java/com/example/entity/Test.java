package com.example.entity;

import randy.framework.annotation.Controller;
import randy.framework.annotation.UrlMapping;

@Controller
public class Test {
    @UrlMapping("/")
    public String accueil() {
        return "accueil"; // → cherche /WEB-INF/views/accueil.jsp (ou .html)
    }

    @UrlMapping(value = "/test/get", method = {"GET"})
    public void test() {
        System.out.println("Résultat de la méthode GET pour /test/get");
    }
    
    @UrlMapping(value = "/test/post", method = {"POST"})
    public void testPost() {
        System.out.println("Résultat de la méthode POST pour /test/post");
    }
}
