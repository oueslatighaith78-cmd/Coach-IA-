package com.coachproductivite.dao;

import com.coachproductivite.model.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.IO;
public class SessionDAO {

    private Connection connexion;

    public SessionDAO() {
        this.connexion = DatabaseConnection.getInstance().getConnexion();
    }

    // Ajouter une session de travail
    public boolean inserer(Session s) {
        String sql = "INSERT INTO session (id_tache, duree) VALUES (?, ?)";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, s.getIdTache());
            ps.setInt(2, s.getDuree());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            IO.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    // Récupérer les sessions d'une tâche
    public List<Session> getSessions(int idTache) {
        List<Session> liste = new ArrayList<>();
        String sql = "SELECT * FROM session WHERE id_tache = ?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, idTache);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Session s = new Session();
                s.setId(rs.getInt("id"));
                s.setIdTache(rs.getInt("id_tache"));
                s.setDuree(rs.getInt("duree"));
                liste.add(s);
            }
        } catch (SQLException e) {
           IO.println("Erreur : " + e.getMessage());
        }
        return liste;
    }
}