package com.coachproductivite.service;

import com.coachproductivite.dao.SessionDAO;
import com.coachproductivite.model.Session;
import java.util.List;

public class SessionService {

    private SessionDAO sessionDAO;

    public SessionService() {
        this.sessionDAO = new SessionDAO();
    }

    // Enregistrer une session de travail
    public boolean enregistrerSession(int idTache, int duree) {
        if (duree <= 0) return false;
        Session s = new Session(idTache, duree);
        return sessionDAO.inserer(s);
    }

    // Récupérer les sessions d'une tâche
    public List<Session> getSessions(int idTache) {
        return sessionDAO.getSessions(idTache);
    }

    // Calculer le temps total passé sur une tâche
    public int getTempsTotalParTache(int idTache) {
        List<Session> sessions = sessionDAO.getSessions(idTache);
        int total = 0;
        for (Session s : sessions) {
            total += s.getDuree();
        }
        return total;
    }
}