package com.coachproductivite.model;

public class Categorie { private int id;
    private String nom;

    public Categorie() {}

    public Categorie(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public String toString() {
        return "Categorie{id=" + id + ", nom=" + nom + "}";
    }
}
