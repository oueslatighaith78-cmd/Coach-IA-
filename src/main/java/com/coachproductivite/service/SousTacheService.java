package com.coachproductivite.service;

import com.coachproductivite.dao.SousTacheDAO;
import com.coachproductivite.model.SousTache;
import com.coachproductivite.model.enums.EtatTache;
import java.util.List;

public class SousTacheService {

    private SousTacheDAO sousTacheDAO;

    public SousTacheService() {
        this.sousTacheDAO = new SousTacheDAO();
    }

    // Ajouter une sous-tâche
    public boolean ajouterSousTache(String titre, int idTache) {
        if (titre.isEmpty()) return false;
        SousTache st = new SousTache(titre, EtatTache.A_FAIRE, idTache);
        return sousTacheDAO.inserer(st);
    }

    // Récupérer les sous-tâches d'une tâche
    public List<SousTache> getSousTaches(int idTache) {
        return sousTacheDAO.getSousTaches(idTache);
    }

    // Terminer une sous-tâche
    public boolean terminerSousTache(SousTache st) {
        st.setEtat(EtatTache.TERMINEE);
        return sousTacheDAO.inserer(st);
    }

    // Supprimer une sous-tâche
    public boolean supprimerSousTache(int id) {
        return sousTacheDAO.supprimer(id);
    }
}