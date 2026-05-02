package com.coachproductivite.service;

import com.coachproductivite.dao.TacheDAO;
import com.coachproductivite.model.Tache;
import com.coachproductivite.model.enums.EtatTache;
import com.coachproductivite.model.enums.Priorite;
import java.time.LocalDate;
import java.util.List;

public class TacheService {

    private TacheDAO tacheDAO;

    public TacheService() {
        this.tacheDAO = new TacheDAO();
    }

    // Créer une tâche
    public boolean creerTache(String titre, String description,
                              Priorite priorite, LocalDate echeance,
                              int idUtilisateur, int idCategorie) {
        if (titre.isEmpty()) {
            System.out.println("Le titre est obligatoire !");
            return false;
        }
        Tache t = new Tache(titre, description, priorite,
                EtatTache.A_FAIRE, echeance,
                idUtilisateur, idCategorie);
        return tacheDAO.inserer(t);
    }

    // Récupérer les tâches d'un utilisateur
    public List<Tache> getTaches(int idUtilisateur) {
        return tacheDAO.getTaches(idUtilisateur);
    }

    // Modifier une tâche
    public boolean modifierTache(Tache t) {
        return tacheDAO.modifier(t);
    }

    // Supprimer une tâche
    public boolean supprimerTache(int id) {
        return tacheDAO.supprimer(id);
    }

    // Marquer une tâche comme terminée
    public boolean terminerTache(int id) {
        Tache t = tacheDAO.trouverParId(id);
        if (t == null) return false;
        t.setEtat(EtatTache.TERMINEE);
        return tacheDAO.modifier(t);
    }
}