package com.example.entity;

import randy.framework.annotation.RController;
import randy.framework.annotation.RUrlMapping;

@RController
public class Test {
    @RUrlMapping("/")
    public String accueil() {
        return "accueil"; // → cherche /WEB-INF/views/accueil.jsp (ou .html)
    }

    @RUrlMapping(value = "/test/get", method = {"GET"})
    public void test() {
        System.out.println("Résultat de la méthode GET pour /test/get");
    }
    
    @RUrlMapping(value = "/test/post", method = {"POST"})
    public void testPost() {
        System.out.println("Résultat de la méthode POST pour /test/post");
    }
}
