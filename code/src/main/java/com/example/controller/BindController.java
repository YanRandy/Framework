// com/example/controller/BindingTestController.java
package com.example.controller;

import java.util.List;
import java.util.Map;

import com.example.entity.Livre;
import com.example.entity.Personne;

import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RMapFile;

@RController
public class BindController {

    // ── Cas primitifs ──────────────────────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/primitifs", method = "GET")
    public Object testPrimitifs(String nom, int age, boolean actif) {
        return Map.of(
                "nom", nom,
                "age", age,
                "actif", actif);
    }

    // ── Cas 1a — String[] ──────────────────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/array", method = "GET")
    public Object testArray(String[] tags) {
        return Map.of("tags", tags, "count", tags != null ? tags.length : 0);
    }

    // ── Cas 1b — List<String> ─────────────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/list", method = "GET")
    public Object testList(List<String> tags) {
        return Map.of("tags", tags, "count", tags.size());
    }

    // ── Cas 1c — int[] ────────────────────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/notes", method = "GET")
    public Object testNotes(int[] notes) {
        int total = 0;
        for (int n : notes)
            total += n;
        return Map.of("notes", notes, "total", total);
    }

    // ── Cas 2 — List<Personne> ────────────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/personnes", method = "POST")
    public Object testListPersonnes(List<Personne> personnes) {
        return Map.of("count", personnes.size(), "personnes", personnes);
    }

    // ── Cas 3 — Objet complexe simple ─────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/personne", method = "GET")
    public Object testObjet(Personne personne) {
        return personne;
    }

    // ── Cas 3b — Deux objets complexes ────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/deux", method = "GET")
    public Object testDeuxObjets(Personne personne, Livre livre) {
        return Map.of("personne", personne, "livre", livre);
    }

    // ── Cas 4 — Map<String, String[]> ─────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/map", method = "GET")
    public Object testMap(Map<String, String[]> filtres) {
        return filtres;
    }

    // ── Cas 5 — Recursivite ────────────────────────────────────

    @RRestApi
    @RUrlMapping(value = "/api/binding/recursif", method = "GET")
    public Object testRecursif(Personne personne) {
        return personne;
    }

    // Cas suivante l'Upload fichier

    // // Un seul fichier
    // @UrlMapping(value = "/upload", method = "POST")
    // public String upload(MapFile photo) {
    //     System.out.println(photo.getFilename()); // "photo.jpg"
    //     System.out.println(photo.getExtension()); // "jpg"
    //     System.out.println(photo.getFile().size() + " bytes");
    //     return "confirmation";
    // }

    // Plusieurs fichiers
    @RUrlMapping(value = "/upload/multi", method = "POST")
    public String uploadMulti(Map<String, RMapFile> fichiers) {
        fichiers.forEach((name, file) -> System.out.println(name + " → " + file.getFilename()));
        return "confirmation";
    }

    // Changer dans BindController
    @RRestApi // ← ajouter
    @RUrlMapping(value = "/upload", method = "POST")
    public Object upload(RMapFile photo) {
        if (photo == null)
            return Map.of("error", "null");
        return Map.of(
                "filename", photo.getFilename(),
                "extension", photo.getExtension(),
                "size", photo.getFile().size());
    }
}
