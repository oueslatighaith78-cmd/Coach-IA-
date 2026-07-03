package com.coachproductivite.controller;

import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.service.UtilisateurService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePwBtn;
    @FXML private Label messageLabel;

    private final UtilisateurService us = new UtilisateurService();
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        passwordField.textProperty().bindBidirectional(passwordVisibleField.textProperty());
    }

    @FXML
    public void togglePassword() {
        passwordVisible = !passwordVisible;
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);
        togglePwBtn.setText(passwordVisible ? "🙈" : "👁");
    }

    @FXML
    public void seConnecter() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String mdp = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || mdp.isEmpty()) {
            msg("Veuillez remplir tous les champs.", false);
            return;
        }

        Utilisateur u = us.connecter(email, mdp);
        if (u == null) {
            msg("Email ou mot de passe incorrect.", false);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_layout.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Sauvegarde l'état actuel de la fenêtre
            double width = stage.getWidth();
            double height = stage.getHeight();
            double x = stage.getX();
            double y = stage.getY();
            boolean maximized = stage.isMaximized();

            // Change juste la racine, garde la même Scene → préserve la taille
            stage.getScene().setRoot(loader.load());

            MainLayoutController ctrl = loader.getController();
            ctrl.setUtilisateur(u);

            // Restaure la taille
            if (maximized) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(width);
                stage.setHeight(height);
                stage.setX(x);
                stage.setY(y);
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg("Erreur de chargement de l'application.", false);
        }
    }

    @FXML
    public void allerInscription() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inscription.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Sauvegarde l'état
            double width = stage.getWidth();
            double height = stage.getHeight();
            double x = stage.getX();
            double y = stage.getY();
            boolean maximized = stage.isMaximized();

            // Change juste la racine
            stage.getScene().setRoot(loader.load());

            // Restaure la taille
            if (maximized) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(width);
                stage.setHeight(height);
                stage.setX(x);
                stage.setY(y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void msg(String text, boolean ok) {
        messageLabel.setStyle("-fx-text-fill:" + (ok ? "#059669" : "#DC2626") +
                ";-fx-font-size:12px;-fx-font-weight:700;");
        messageLabel.setText(text);
    }
}