package com.coachproductivite.dao;

import com.coachproductivite.model.SousTache;
import com.coachproductivite.model.enums.EtatTache;

import java.lang.IO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.IO;
public class SousTacheDAO {

    private Connection connexion;

    public SousTacheDAO() {
        this.connexion = DatabaseConnection.getInstance().getConnexion();
    }

    // Ajouter une sous-tâche
    public boolean inserer(SousTache st) {
        String sql = "INSERT INTO sous_tache (titre, etat, id_tache) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setString(1, st.getTitre());
            ps.setString(2, st.getEtat().name());
            ps.setInt(3, st.getIdTache());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    // Récupérer les sous-tâches d'une tâche
    public List<SousTache> getSousTaches(int idTache) {
        List<SousTache> liste = new ArrayList<>();
        String sql = "SELECT * FROM sous_tache WHERE id_tache = ?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, idTache);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SousTache st = new SousTache();
                st.setId(rs.getInt("id"));
                st.setTitre(rs.getString("titre"));
                st.setEtat(EtatTache.valueOf(rs.getString("etat")));
                st.setIdTache(rs.getInt("id_tache"));
                liste.add(st);
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return liste;
    }

    // Supprimer une sous-tâche
    public boolean supprimer(int id) {
        String sql = "DELETE FROM sous_tache WHERE id=?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            IO.println("Erreur : " + e.getMessage());
            return false;
        }
    }
}
