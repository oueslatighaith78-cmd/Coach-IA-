package com.coachproductivite.dao;

import com.coachproductivite.model.Tache;
import com.coachproductivite.model.enums.EtatTache;
import com.coachproductivite.model.enums.Priorite;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TacheDAO {

    private Connection connexion;

    public TacheDAO() {
        this.connexion = DatabaseConnection.getInstance().getConnexion();
    }

    public boolean inserer(Tache t) {
        String sql =
                "INSERT INTO tache " +
                        "(titre, description, priorite, etat, " +
                        "echeance, id_utilisateur, id_categorie) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps =
                     connexion.prepareStatement(sql)) {

            ps.setString(1, t.getTitre());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getPriorite().name());
            ps.setString(4, t.getEtat().name());

            // ✅ CETTE PARTIE EST CRITIQUE
            if (t.getEcheance() != null) {
                ps.setDate(5, Date.valueOf(t.getEcheance()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setInt(6, t.getIdUtilisateur());
            ps.setInt(7, t.getIdCategorie());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Erreur inserer : " + e.getMessage());
            return false;
        }
    }

    public List<Tache> getTaches(int idUtilisateur) {
        List<Tache> liste = new ArrayList<>();
        String sql = "SELECT * FROM tache WHERE id_utilisateur = ?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getTaches : " + e.getMessage());
            e.printStackTrace();
        }
        return liste;
    }

    public boolean modifier(Tache t) {
        String sql = "UPDATE tache SET titre=?, description=?, priorite=?, etat=?, echeance=?, id_categorie=? WHERE id=?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setString(1, t.getTitre());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getPriorite().name());
            ps.setString(4, t.getEtat().name());
            if (t.getEcheance() != null) {
                ps.setDate(5, Date.valueOf(t.getEcheance()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setInt(6, t.getIdCategorie());
            ps.setInt(7, t.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur modification : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM tache WHERE id=?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur suppression : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Tache trouverParId(int id) {
        String sql = "SELECT * FROM tache WHERE id = ?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapper(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur trouverParId : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private Tache mapper(ResultSet rs) throws SQLException {
        Tache t = new Tache();
        t.setId(rs.getInt("id"));
        t.setTitre(rs.getString("titre"));
        t.setDescription(rs.getString("description"));
        t.setPriorite(Priorite.valueOf(rs.getString("priorite")));
        t.setEtat(EtatTache.valueOf(rs.getString("etat")));
        java.sql.Date sqlDate = rs.getDate("echeance");
        if (sqlDate != null) {
            t.setEcheance(sqlDate.toLocalDate());
        }
        t.setIdUtilisateur(rs.getInt("id_utilisateur"));
        t.setIdCategorie(rs.getInt("id_categorie"));
        return t;
    }
}