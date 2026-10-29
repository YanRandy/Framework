package com.example.entity;

/**
 * Entité Livre (table {@code livres}, voir framework/librairie.sql).
 */
public class RLivre {
    private int id;
    private String titre;
    private String auteur;

    /** Constructeur vide obligatoire : le framework instancie l'objet par réflexion. */
    public RLivre() {
    }

    public RLivre(int id, String titre, String auteur) {
        this.id = id;
        this.titre = titre;
        this.auteur = auteur;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }
}
