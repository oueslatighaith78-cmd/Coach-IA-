package com.coachproductivite.service;

import com.coachproductivite.dao.StatistiquesDAO;
import com.coachproductivite.dao.TacheDAO;
import com.coachproductivite.model.Statistiques;
import com.coachproductivite.model.Tache;
import com.coachproductivite.model.enums.EtatTache;
import java.util.List;

public class StatistiquesService {

    private StatistiquesDAO statistiquesDAO;
    private TacheDAO tacheDAO;

    public StatistiquesService() {
        this.statistiquesDAO = new StatistiquesDAO();
        this.tacheDAO = new TacheDAO();
    }

    // Calculer et sauvegarder les stats d'un utilisateur
    public Statistiques calculerStats(int idUtilisateur) {
        List<Tache> taches = tacheDAO.getTaches(idUtilisateur);
        int total = taches.size();
        int terminees = 0;
        for (Tache t : taches) {
            if (t.getEtat() == EtatTache.TERMINEE) {
                terminees++;
            }
        }
        Statistiques s = new Statistiques(idUtilisateur);
        s.setTachesTotal(total);
        s.setTachesTerminees(terminees);
        statistiquesDAO.inserer(s);
        return s;
    }

    // Récupérer les stats d'un utilisateur
    public Statistiques getStats(int idUtilisateur) {
        return statistiquesDAO.getStats(idUtilisateur);
    }
}
