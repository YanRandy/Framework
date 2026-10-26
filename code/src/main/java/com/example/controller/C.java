package com.example.controller;
import randy.framework.annotation.RController;
import randy.framework.annotation.RUrlMapping;

@RController    //annotation
public class C {
    @RUrlMapping("/oily")
    public void afficherOily() {
    }

}
