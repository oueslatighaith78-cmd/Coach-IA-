package com.coachproductivite.dao;

import com.coachproductivite.model.Statistiques;
import java.sql.*;

public class StatistiquesDAO {

    private Connection connexion;

    public StatistiquesDAO() {
        this.connexion = DatabaseConnection.getInstance().getConnexion();
    }

    // Ajouter des statistiques
    public boolean inserer(Statistiques s) {
        String sql = "INSERT INTO statistiques (id_utilisateur, taches_total, taches_terminees) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, s.getIdUtilisateur());
            ps.setInt(2, s.getTachesTotal());
            ps.setInt(3, s.getTachesTerminees());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    // Récupérer les stats d'un utilisateur
    public Statistiques getStats(int idUtilisateur) {
        String sql = "SELECT * FROM statistiques WHERE id_utilisateur = ?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Statistiques s = new Statistiques();
                s.setId(rs.getInt("id"));
                s.setIdUtilisateur(rs.getInt("id_utilisateur"));
                s.setTachesTotal(rs.getInt("taches_total"));
                s.setTachesTerminees(rs.getInt("taches_terminees"));
                return s;
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return null;
    }

    // Mettre à jour les stats
    public boolean modifier(Statistiques s) {
        String sql = "UPDATE statistiques SET taches_total=?, taches_terminees=? WHERE id_utilisateur=?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, s.getTachesTotal());
            ps.setInt(2, s.getTachesTerminees());
            ps.setInt(3, s.getIdUtilisateur());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            IO.readln("Erreur : " + e.getMessage());
            return false;
        }
    }
}