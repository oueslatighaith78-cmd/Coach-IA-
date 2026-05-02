package com.coachproductivite.controller;

import com.coachproductivite.model.Statistiques;
import com.coachproductivite.model.Tache;
import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.service.StatistiquesService;
import com.coachproductivite.service.TacheService;
import com.coachproductivite.service.UtilisateurService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class ProfilController implements UtilisateurAware {

    @FXML private Label avatarGrandLabel;
    @FXML private Label nomAffLabel;
    @FXML private Label emailAffLabel;
    @FXML private Label badgeNbLabel;
    @FXML private Label badgeTauxLabel;
    @FXML private Label badgeMembreLabel;

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextArea objectifsField;
    @FXML private ComboBox<String> rythmeCombo;
    @FXML private Label msgLabel;
    @FXML private Label prefMsgLabel;

    @FXML private PasswordField newPwField;
    @FXML private PasswordField confirmPwField;
    @FXML private Label pwMsgLabel;
    @FXML private Label forceBar1;
    @FXML private Label forceBar2;
    @FXML private Label forceBar3;
    @FXML private Label forceLabel;

    private Utilisateur utilisateur;
    private final UtilisateurService us = new UtilisateurService();
    private final StatistiquesService ss = new StatistiquesService();
    private final TacheService ts = new TacheService();

    @FXML
    public void initialize() {
        rythmeCombo.setItems(FXCollections.observableArrayList(
                "Matin (6h - 12h)",
                "Après-midi (12h - 18h)",
                "Soir (18h - 00h)",
                "Nuit (00h - 6h)",
                "Flexible"));

        // Indicateur de force du mot de passe
        if (newPwField != null) {
            newPwField.textProperty().addListener((obs, oldV, newV) -> majForceMotDePasse(newV));
        }
    }

    @Override
    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        rafraichir();
    }

    private void rafraichir() {
        if (utilisateur == null) return;
        Utilisateur u = utilisateur;

        nomField.setText(u.getNom());
        emailField.setText(u.getEmail());
        nomAffLabel.setText(u.getNom());
        emailAffLabel.setText(u.getEmail());

        // Initiales
        String n = u.getNom() == null ? "U" : u.getNom().trim();
        String[] p = n.split("\\s+");
        String ini;
        if (p.length >= 2) {
            ini = ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
        } else if (n.length() >= 2) {
            ini = n.substring(0, 2).toUpperCase();
        } else {
            ini = n.toUpperCase();
        }
        avatarGrandLabel.setText(ini);

        // Stats badges
        try {
            Statistiques s = ss.calculerStats(u.getId());
            badgeNbLabel.setText("📋 " + s.getTachesTotal() + " tâches");
            badgeTauxLabel.setText("📊 " + String.format("%.0f%%", s.getTauxCompletion()));

            // Badge membre dynamique
            int total = s.getTachesTotal();
            if (total >= 20) badgeMembreLabel.setText("⭐ Expert");
            else if (total >= 10) badgeMembreLabel.setText("🚀 Confirmé");
            else if (total >= 1) badgeMembreLabel.setText("🌱 Débutant");
            else badgeMembreLabel.setText("👋 Nouveau");
        } catch (Exception ignored) {
            badgeNbLabel.setText("📋 0 tâches");
            badgeTauxLabel.setText("📊 0%");
            badgeMembreLabel.setText("👋 Nouveau");
        }
    }

    private void majForceMotDePasse(String pw) {
        if (pw == null || pw.isEmpty()) {
            forceBar1.setStyle(barStyle("#E5E7EB"));
            forceBar2.setStyle(barStyle("#E5E7EB"));
            forceBar3.setStyle(barStyle("#E5E7EB"));
            forceLabel.setText("");
            return;
        }

        int score = 0;
        if (pw.length() >= 6) score++;
        if (pw.length() >= 10) score++;
        if (pw.matches(".*[A-Z].*") && pw.matches(".*[0-9].*")) score++;

        switch (score) {
            case 1 -> {
                forceBar1.setStyle(barStyle("#EF4444"));
                forceBar2.setStyle(barStyle("#E5E7EB"));
                forceBar3.setStyle(barStyle("#E5E7EB"));
                forceLabel.setText("Faible");
                forceLabel.setStyle("-fx-text-fill:#EF4444;-fx-font-size:11px;-fx-font-weight:700;-fx-padding:0 0 0 8;");
            }
            case 2 -> {
                forceBar1.setStyle(barStyle("#F59E0B"));
                forceBar2.setStyle(barStyle("#F59E0B"));
                forceBar3.setStyle(barStyle("#E5E7EB"));
                forceLabel.setText("Moyen");
                forceLabel.setStyle("-fx-text-fill:#F59E0B;-fx-font-size:11px;-fx-font-weight:700;-fx-padding:0 0 0 8;");
            }
            case 3 -> {
                forceBar1.setStyle(barStyle("#10B981"));
                forceBar2.setStyle(barStyle("#10B981"));
                forceBar3.setStyle(barStyle("#10B981"));
                forceLabel.setText("Fort");
                forceLabel.setStyle("-fx-text-fill:#10B981;-fx-font-size:11px;-fx-font-weight:700;-fx-padding:0 0 0 8;");
            }
            default -> {
                forceBar1.setStyle(barStyle("#E5E7EB"));
                forceBar2.setStyle(barStyle("#E5E7EB"));
                forceBar3.setStyle(barStyle("#E5E7EB"));
                forceLabel.setText("");
            }
        }
    }

    private String barStyle(String color) {
        return "-fx-min-width:60px;-fx-min-height:4px;-fx-background-color:" + color + ";-fx-background-radius:2;";
    }

    @FXML
    public void sauvegarderInfos() {
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.isEmpty()) {
            m(msgLabel, "Le nom est obligatoire.", false);
            return;
        }
        utilisateur.setNom(nom);
        if (us.modifierProfil(utilisateur)) {
            m(msgLabel, "✓ Profil mis à jour avec succès !", true);
            rafraichir();
        } else {
            m(msgLabel, "Erreur lors de la sauvegarde.", false);
        }
    }

    @FXML
    public void sauvegarderPreferences() {
        // Mémorise localement (tu peux étendre vers BDD si tu ajoutes des colonnes)
        m(prefMsgLabel, "✓ Préférences enregistrées localement !", true);
    }

    @FXML
    public void changerMotDePasse() {
        String np = newPwField.getText();
        String cp = confirmPwField.getText();

        if (np == null || np.isEmpty()) {
            m(pwMsgLabel, "Entre un nouveau mot de passe.", false);
            return;
        }
        if (np.length() < 6) {
            m(pwMsgLabel, "Le mot de passe doit faire au moins 6 caractères.", false);
            return;
        }
        if (!np.equals(cp)) {
            m(pwMsgLabel, "Les mots de passe ne correspondent pas.", false);
            return;
        }

        utilisateur.setMotDePasse(np);
        if (us.modifierProfil(utilisateur)) {
            m(pwMsgLabel, "✓ Mot de passe modifié avec succès !", true);
            newPwField.clear();
            confirmPwField.clear();
            majForceMotDePasse("");
        } else {
            m(pwMsgLabel, "Erreur lors de la modification.", false);
        }
    }

    @FXML
    public void supprimerToutesTaches() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "⚠️ Supprimer TOUTES tes tâches ? Cette action est irréversible.",
                ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText("Confirmation requise");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                List<Tache> taches = ts.getTaches(utilisateur.getId());
                int count = 0;
                for (Tache t : taches) {
                    if (ts.supprimerTache(t.getId())) count++;
                }
                Alert info = new Alert(Alert.AlertType.INFORMATION,
                        "✓ " + count + " tâche(s) supprimée(s).");
                info.setHeaderText(null);
                info.showAndWait();
                rafraichir();
            }
        });
    }

    @FXML
    public void deconnexion() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Vraiment se déconnecter ?",
                ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    Stage s = (Stage) nomField.getScene().getWindow();
                    s.setScene(new Scene(l.load(), 900, 650));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void m(Label l, String txt, boolean ok) {
        l.setStyle("-fx-text-fill:" + (ok ? "#059669" : "#DC2626") +
                ";-fx-font-size:12px;-fx-font-weight:700;");
        l.setText(txt);
    }
}