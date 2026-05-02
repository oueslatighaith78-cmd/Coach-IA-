package com.coachproductivite.dao;

import com.coachproductivite.model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.IO;
public class UtilisateurDAO {

    private Connection connexion;

    public UtilisateurDAO() {
        this.connexion = DatabaseConnection.getInstance().getConnexion();
    }

    // INSERT
    public boolean inserer(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (nom, email, mot_de_passe) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getMotDePasse());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            IO.println("Erreur insertion utilisateur : " + e.getMessage());
            return false;
        }
    }

    // ── SELECT par email (pour la connexion) ────────────
    public Utilisateur trouverParEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe")
                );
            }
        } catch (SQLException e) {
            IO.println("Erreur recherche utilisateur : " + e.getMessage());
        }
        return null;
    }

    // ── SELECT par id ───────────────────────────────────
    public Utilisateur trouverParId(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe")
                );
            }
        } catch (SQLException e) {
            IO.println("Erreur recherche par id : " + e.getMessage());
        }
        return null;
    }

    // ── SELECT tous ─────────────────────────────────────
    public List<Utilisateur> tousLesUtilisateurs() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";
        try {
            Statement st = connexion.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                liste.add(new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe")
                ));
            }
        } catch (SQLException e) {
            IO.println("Erreur liste utilisateurs : " + e.getMessage());
        }
        return liste;
    }

    // ── UPDATE ──────────────────────────────────────────
    public boolean modifier(Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom=?, email=?, mot_de_passe=? WHERE id=?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getMotDePasse());
            ps.setInt(4, u.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            IO.println("Erreur modification utilisateur : " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ──────────────────────────────────────────
    public boolean supprimer(int id) {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try {
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            IO.println("Erreur suppression utilisateur : " + e.getMessage());
            return false;
        }
    }
}