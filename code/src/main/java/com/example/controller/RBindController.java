package com.example.controller;

import java.util.List;
import java.util.Map;

import com.example.entity.RLivre;
import com.example.entity.RPersonne;

import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RMapFile;

/**
 * Tests du binding des paramètres (Sprints 6 à 8).
 * Chaque route renvoie en JSON ce que le framework a reçu, pour vérifier
 * que la requête a bien été convertie dans le type Java déclaré.
 */
@RController
public class RBindController {

    /** ?nom=Rakoto&age=20&actif=true → String, int, boolean convertis automatiquement. */
    @RRestApi
    @RUrlMapping(value = "/api/binding/primitifs", method = "GET")
    public Object testPrimitifs(String nom, int age, boolean actif) {
        return Map.of(
                "nom", nom,
                "age", age,
                "actif", actif);
    }

    /** ?tags=java&tags=web → String[] (même nom de paramètre répété). */
    @RRestApi
    @RUrlMapping(value = "/api/binding/array", method = "GET")
    public Object testArray(String[] tags) {
        return Map.of("tags", tags, "count", tags != null ? tags.length : 0);
    }

    /** Même chose qu'au-dessus mais en List<String> (type lu dans le générique). */
    @RRestApi
    @RUrlMapping(value = "/api/binding/list", method = "GET")
    public Object testList(List<String> tags) {
        return Map.of("tags", tags, "count", tags.size());
    }

    /** ?notes=12&notes=15 → int[] (chaque valeur convertie en int). */
    @RRestApi
    @RUrlMapping(value = "/api/binding/notes", method = "GET")
    public Object testNotes(int[] notes) {
        int total = 0;
        for (int n : notes)
            total += n;
        return Map.of("notes", notes, "total", total);
    }

    /** personnes=x&personnes=y&personnes[0].nom=..&personnes[1].nom=.. → List<RPersonne> indexée. */
    @RRestApi
    @RUrlMapping(value = "/api/binding/personnes", method = "POST")
    public Object testListPersonnes(List<RPersonne> personnes) {
        return Map.of("count", personnes.size(), "personnes", personnes);
    }

    /** Un seul objet → pas de préfixe : ?nom=..&prenom=..&age=.. */
    @RRestApi
    @RUrlMapping(value = "/api/binding/personne", method = "GET")
    public Object testObjet(RPersonne personne) {
        return personne;
    }

    /** Deux objets → préfixe obligatoire : ?personne.nom=..&livre.titre=.. */
    @RRestApi
    @RUrlMapping(value = "/api/binding/deux", method = "GET")
    public Object testDeuxObjets(RPersonne personne, RLivre livre) {
        return Map.of("personne", personne, "livre", livre);
    }

    /** Map<String, String[]> → tous les paramètres de la requête, bruts. */
    @RRestApi
    @RUrlMapping(value = "/api/binding/map", method = "GET")
    public Object testMap(Map<String, String[]> filtres) {
        return filtres;
    }

    /** RPersonne contient un RPersonne (ami) : ?nom=..&ami.nom=.. sans boucle infinie. */
    @RRestApi
    @RUrlMapping(value = "/api/binding/recursif", method = "GET")
    public Object testRecursif(RPersonne personne) {
        return personne;
    }

    /** Formulaire de test des uploads (vue test-upload.jsp). */
    @RUrlMapping(value = "/upload", method = "GET")
    public String uploadForm() {
        return "test-upload";
    }

    /** Upload d'un fichier (multipart, champ "photo") → RMapFile. */
    @RRestApi
    @RUrlMapping(value = "/upload", method = "POST")
    public Object upload(RMapFile photo) {
        if (photo == null)
            return Map.of("error", "null");
        return Map.of(
                "filename", photo.getFilename(),
                "extension", photo.getExtension(),
                "size", photo.getFile().size());
    }

    /** Upload de plusieurs fichiers → Map<nomDuChamp, RMapFile>. */
    @RUrlMapping(value = "/upload/multi", method = "POST")
    public String uploadMulti(Map<String, RMapFile> fichiers) {
        fichiers.forEach((name, file) -> System.out.println(name + " → " + file.getFilename()));
        return "confirmation";
    }
}
