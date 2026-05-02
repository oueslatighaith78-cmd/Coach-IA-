package com.coachproductivite.model;
public class Statistiques {

    private int id;
    private int idUtilisateur;
    private int tachesTotal;
    private int tachesTerminees;

    public Statistiques() {}

    public Statistiques(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
        this.tachesTotal = 0;
        this.tachesTerminees = 0;
    }

    public Statistiques(int id, int idUtilisateur,
                        int tachesTotal, int tachesTerminees) {
        this.id = id;
        this.idUtilisateur = idUtilisateur;
        this.tachesTotal = tachesTotal;
        this.tachesTerminees = tachesTerminees;
    }

    public double getTauxCompletion() {
        if (tachesTotal == 0) return 0;
        return (double) tachesTerminees / tachesTotal * 100;
    }

    public int getId() { return id; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public int getTachesTotal() { return tachesTotal; }
    public int getTachesTerminees() { return tachesTerminees; }

    public void setId(int id) { this.id = id; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public void setTachesTotal(int tachesTotal) { this.tachesTotal = tachesTotal; }
    public void setTachesTerminees(int tachesTerminees) { this.tachesTerminees = tachesTerminees; }

    @Override
    public String toString() {
        return "Statistiques{total=" + tachesTotal +
                ", terminees=" + tachesTerminees +
                ", taux=" + getTauxCompletion() + "%}";
    }
}