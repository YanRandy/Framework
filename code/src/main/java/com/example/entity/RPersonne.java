package com.example.entity;

/**
 * Entité Personne (table {@code Personne}).
 * Sert aussi à tester le binding d'objet : les setters sont remplis
 * automatiquement depuis les paramètres de la requête (?nom=..&prenom=..&age=..).
 */
public class RPersonne {
    private int id;
    private String nom;
    private String prenom;
    private int age;
    /** Auto-référence : teste le binding imbriqué (?ami.nom=..) et la protection contre la boucle infinie. */
    RPersonne ami;

    /** Constructeur vide obligatoire : le framework instancie l'objet par réflexion. */
    public RPersonne() {
    }

    public RPersonne(String nom, String prenom, int age) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public RPersonne getAmi() {
        return ami;
    }

    public void setAmi(RPersonne ami) {
        this.ami = ami;
    }
}
