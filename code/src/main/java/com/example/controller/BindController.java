// com/example/controller/BindingTestController.java
package com.example.controller;

import java.util.List;
import java.util.Map;

import com.example.entity.Personne;

import randy.framework.annotation.Controller;
import randy.framework.annotation.RestApi;
import randy.framework.annotation.UrlMapping;

import com.example.entity.Livre;

@Controller
public class BindController {

    // ── Cas primitifs ──────────────────────────────────────────────

    @RestApi
    @UrlMapping(value = "/api/binding/primitifs", method = "GET")
    public Object testPrimitifs(String nom, int age, boolean actif) {
        return Map.of(
            "nom",   nom,
            "age",   age,
            "actif", actif
        );
    }

    // ── Cas 1a — String[] ──────────────────────────────────────────

    @RestApi
    @UrlMapping(value = "/api/binding/array", method = "GET")
    public Object testArray(String[] tags) {
        return Map.of("tags", tags, "count", tags != null ? tags.length : 0);
    }

    // ── Cas 1b — List<String> ─────────────────────────────────────

    @RestApi
    @UrlMapping(value = "/api/binding/list", method = "GET")
    public Object testList(List<String> tags) {
        return Map.of("tags", tags, "count", tags.size());
    }

    // ── Cas 1c — int[] ────────────────────────────────────────────

    @RestApi
    @UrlMapping(value = "/api/binding/notes", method = "GET")
    public Object testNotes(int[] notes) {
        int total = 0;
        for (int n : notes) total += n;
        return Map.of("notes", notes, "total", total);
    }

    // ── Cas 2 — List<Personne> ────────────────────────────────────

    @RestApi
    @UrlMapping(value = "/api/binding/personnes", method = "POST")
    public Object testListPersonnes(List<Personne> personnes) {
        return Map.of("count", personnes.size(), "personnes", personnes);
    }

    // ── Cas 3 — Objet complexe simple ─────────────────────────────

    @RestApi
    @UrlMapping(value = "/api/binding/personne", method = "GET")
    public Object testObjet(Personne personne) {
        return personne;
    }

    // ── Cas 3b — Deux objets complexes ────────────────────────────

    @RestApi
    @UrlMapping(value = "/api/binding/deux", method = "GET")
    public Object testDeuxObjets(Personne personne, Livre livre) {
        return Map.of("personne", personne, "livre", livre);
    }

    // ── Cas 4 — Map<String, String[]> ─────────────────────────────

    @RestApi
    @UrlMapping(value = "/api/binding/map", method = "GET")
    public Object testMap(Map<String, String[]> filtres) {
        return filtres;
    }
}
