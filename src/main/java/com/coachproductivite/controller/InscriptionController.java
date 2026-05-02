package com.coachproductivite.controller;

import com.coachproductivite.service.UtilisateurService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class InscriptionController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePwBtn;
    @FXML private Label messageLabel;

    @FXML private HBox forceContainer;
    @FXML private Label forceBar1;
    @FXML private Label forceBar2;
    @FXML private Label forceBar3;
    @FXML private Label forceLabel;

    private final UtilisateurService us = new UtilisateurService();
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        passwordField.textProperty().bindBidirectional(passwordVisibleField.textProperty());
        passwordField.textProperty().addListener((obs, oldV, newV) -> majForceMotDePasse(newV));
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

    private void majForceMotDePasse(String pw) {
        if (pw == null || pw.isEmpty()) {
            forceContainer.setVisible(false);
            forceContainer.setManaged(false);
            return;
        }

        forceContainer.setVisible(true);
        forceContainer.setManaged(true);

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
                forceLabel.setStyle("-fx-text-fill:#EF4444;-fx-font-size:10px;-fx-font-weight:700;-fx-padding:0 0 0 6;");
            }
            case 2 -> {
                forceBar1.setStyle(barStyle("#F59E0B"));
                forceBar2.setStyle(barStyle("#F59E0B"));
                forceBar3.setStyle(barStyle("#E5E7EB"));
                forceLabel.setText("Moyen");
                forceLabel.setStyle("-fx-text-fill:#F59E0B;-fx-font-size:10px;-fx-font-weight:700;-fx-padding:0 0 0 6;");
            }
            case 3 -> {
                forceBar1.setStyle(barStyle("#10B981"));
                forceBar2.setStyle(barStyle("#10B981"));
                forceBar3.setStyle(barStyle("#10B981"));
                forceLabel.setText("Fort");
                forceLabel.setStyle("-fx-text-fill:#10B981;-fx-font-size:10px;-fx-font-weight:700;-fx-padding:0 0 0 6;");
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
        return "-fx-min-width:80px;-fx-min-height:4px;-fx-background-color:" + color + ";-fx-background-radius:2;";
    }

    @FXML
    public void sInscrire() {
        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String mdp = passwordField.getText() == null ? "" : passwordField.getText();

        if (nom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            msg("Tous les champs sont obligatoires.", false);
            return;
        }
        if (mdp.length() < 6) {
            msg("Le mot de passe doit faire au moins 6 caractères.", false);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            msg("L'adresse email n'est pas valide.", false);
            return;
        }

        boolean ok = us.inscrire(nom, email, mdp);
        if (ok) {
            msg("✓ Compte créé ! Redirection vers la connexion...", true);
            new Thread(() -> {
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                Platform.runLater(this::allerLogin);
            }).start();
        } else {
            msg("Email déjà utilisé ou erreur lors de l'inscription.", false);
        }
    }

    @FXML
    public void allerLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();
            boolean maximized = stage.isMaximized();

            Scene newScene = new Scene(loader.load());
            stage.setScene(newScene);

            if (maximized) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(width);
                stage.setHeight(height);
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