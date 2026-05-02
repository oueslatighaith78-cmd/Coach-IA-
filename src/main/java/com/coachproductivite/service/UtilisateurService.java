package com.coachproductivite.service;

import com.coachproductivite.dao.UtilisateurDAO;
import com.coachproductivite.model.Utilisateur;

public class UtilisateurService {

    private UtilisateurDAO utilisateurDAO;

    public UtilisateurService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    // Inscription
    public boolean inscrire(String nom, String email, String motDePasse) {
        // Vérifier que les champs ne sont pas vides
        if (nom.isEmpty() || email.isEmpty() || motDePasse.isEmpty()) {
            System.out.println("Tous les champs sont obligatoires !");
            return false;
        }
        // Vérifier que l'email n'existe pas déjà
        if (utilisateurDAO.trouverParEmail(email) != null) {
            System.out.println("Cet email est déjà utilisé !");
            return false;
        }
        // Créer et insérer l'utilisateur
        Utilisateur u = new Utilisateur(nom, email, motDePasse);
        return utilisateurDAO.inserer(u);
    }

    // Connexion
    public Utilisateur connecter(String email, String motDePasse) {
        Utilisateur u = utilisateurDAO.trouverParEmail(email);
        if (u == null) {
            System.out.println("Email introuvable !");
            return null;
        }
        if (!u.getMotDePasse().equals(motDePasse)) {
            System.out.println("Mot de passe incorrect !");
            return null;
        }
        return u;
    }

    // Modifier profil
    public boolean modifierProfil(Utilisateur u) {
        return utilisateurDAO.modifier(u);
    }
}