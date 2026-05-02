package com.coachproductivite.controller;

import com.coachproductivite.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainLayoutController {

    @FXML private Label nomLabel;
    @FXML private Label avatarLabel;
    @FXML private StackPane contenuPrincipal;
    @FXML private Button btnDashboard;
    @FXML private Button btnTaches;
    @FXML private Button btnChrono;
    @FXML private Button btnCalendrier;
    @FXML private Button btnIA;
    @FXML private Button btnStats;
    @FXML private Button btnProfil;

    private Utilisateur utilisateur;

    private static final String ACTIF =
            "-fx-background-color:#4F46E5;" +
                    "-fx-text-fill:white;-fx-font-weight:bold;" +
                    "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                    "-fx-padding:11 18;-fx-border-radius:10;" +
                    "-fx-background-radius:10;-fx-cursor:hand;" +
                    "-fx-pref-height:46px;" +
                    "-fx-effect:dropshadow(gaussian,rgba(79,70,229,0.5),12,0,0,3);";

    private static final String NORMAL =
            "-fx-background-color:transparent;" +
                    "-fx-text-fill:#A0A8C0;-fx-font-size:13px;" +
                    "-fx-alignment:CENTER-LEFT;-fx-padding:11 18;" +
                    "-fx-border-radius:10;-fx-background-radius:10;" +
                    "-fx-cursor:hand;-fx-pref-height:46px;" +
                    "-fx-border-color:transparent;";

    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;

        String nom = (u != null && u.getNom() != null && !u.getNom().isBlank())
                ? u.getNom().trim()
                : "Utilisateur";

        nomLabel.setText(nom);

        String[] p = nom.split("\\s+");
        String ini = p.length >= 2
                ? ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase()
                : (nom.length() >= 2 ? nom.substring(0,2).toUpperCase() : nom.substring(0,1).toUpperCase());

        avatarLabel.setText(ini);

        allerDashboard();
    }

    private void charger(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Node page = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof UtilisateurAware ua) {
                ua.setUtilisateur(utilisateur);
            }

            contenuPrincipal.getChildren().setAll(page);

        } catch (Exception e) {
            System.out.println("Erreur de chargement de : " + path);
            System.out.println(e.getMessage());
            e.printStackTrace();
            contenuPrincipal.getChildren().clear();
        }
    }

    private void actif(Button b) {
        for (Button btn : new Button[]{
                btnDashboard, btnTaches, btnChrono,
                btnCalendrier, btnIA, btnStats, btnProfil
        }) {
            if (btn != null) btn.setStyle(NORMAL);
        }
        if (b != null) b.setStyle(ACTIF);
    }

    @FXML public void allerDashboard() {
        actif(btnDashboard);
        charger("/fxml/dashboard_content.fxml");
    }
    @FXML public void allerTaches() {
        actif(btnTaches);
        charger("/fxml/taches_content.fxml");
    }
    @FXML public void allerChrono() {
        actif(btnChrono);
        charger("/fxml/chronometre_content.fxml");
    }
    @FXML public void allerCalendrier() {
        actif(btnCalendrier);
        charger("/fxml/calendrier_content.fxml");
    }
    @FXML public void allerIA() {
        actif(btnIA);
        charger("/fxml/ia_content.fxml");
    }
    @FXML public void allerStats() {
        actif(btnStats);
        charger("/fxml/stats_content.fxml");
    }
    @FXML public void allerProfil() {
        actif(btnProfil);
        charger("/fxml/profil_content.fxml");
    }

    @FXML public void seDeconnecter() {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage s = (Stage) contenuPrincipal.getScene().getWindow();
            s.setScene(new Scene(l.load(), 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}