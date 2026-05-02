package com.coachproductivite.model;
import com.coachproductivite.model.enums.EtatTache;
import com.coachproductivite.model.enums.Priorite;
import java.time.LocalDate;

public class Tache {

    private int id;
    private String titre;
    private String description;
    private Priorite priorite;
    private EtatTache etat;
    private LocalDate echeance;
    private int idUtilisateur;
    private int idCategorie;

    public Tache() {}

    public Tache(String titre, String description, Priorite priorite,
                 EtatTache etat, LocalDate echeance,
                 int idUtilisateur, int idCategorie) {
        this.titre = titre;
        this.description = description;
        this.priorite = priorite;
        this.etat = etat;
        this.echeance = echeance;
        this.idUtilisateur = idUtilisateur;
        this.idCategorie = idCategorie;
    }

    public Tache(int id, String titre, String description, Priorite priorite,
                 EtatTache etat, LocalDate echeance,
                 int idUtilisateur, int idCategorie) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.priorite = priorite;
        this.etat = etat;
        this.echeance = echeance;
        this.idUtilisateur = idUtilisateur;
        this.idCategorie = idCategorie;
    }

    public int getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public Priorite getPriorite() { return priorite; }
    public EtatTache getEtat() { return etat; }
    public LocalDate getEcheance() { return echeance; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public int getIdCategorie() { return idCategorie; }

    public void setId(int id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setDescription(String description) { this.description = description; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }
    public void setEtat(EtatTache etat) { this.etat = etat; }
    public void setEcheance(LocalDate echeance) { this.echeance = echeance; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public void setIdCategorie(int idCategorie) { this.idCategorie = idCategorie; }

    @Override
    public String toString() {
        return "Tache{id=" + id + ", titre=" + titre +
                ", priorite=" + priorite + ", etat=" + etat + "}";
    }
}