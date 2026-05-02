package com.coachproductivite.model;
import com.coachproductivite.model.enums.EtatTache;

public class SousTache {

    private int id;
    private String titre;
    private EtatTache etat;
    private int idTache;

    public SousTache() {}

    public SousTache(String titre, EtatTache etat, int idTache) {
        this.titre = titre;
        this.etat = etat;
        this.idTache = idTache;
    }

    public SousTache(int id, String titre, EtatTache etat, int idTache) {
        this.id = id;
        this.titre = titre;
        this.etat = etat;
        this.idTache = idTache;
    }

    public int getId() { return id; }
    public String getTitre() { return titre; }
    public EtatTache getEtat() { return etat; }
    public int getIdTache() { return idTache; }

    public void setId(int id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setEtat(EtatTache etat) { this.etat = etat; }
    public void setIdTache(int idTache) { this.idTache = idTache; }

    @Override
    public String toString() {
        return "SousTache{id=" + id + ", titre=" + titre + ", etat=" + etat + "}";
    }
}
