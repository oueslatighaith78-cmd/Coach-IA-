package com.coachproductivite.controller;

import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.model.enums.Priorite;
import com.coachproductivite.service.TacheService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NouvelleTacheController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> prioriteCombo;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private DatePicker datePicker;
    @FXML private Label messageLabel;

    private final TacheService ts = new TacheService();
    private Utilisateur utilisateur;
    private Runnable onSuccess;

    private static final String[] CATS = {"Études", "Travail", "Personnel"};

    @FXML
    public void initialize() {
        prioriteCombo.setItems(FXCollections.observableArrayList(
                "HAUTE", "MOYENNE", "FAIBLE"
        ));
        categorieCombo.setItems(FXCollections.observableArrayList(CATS));

        // Styles dynamiques sur priorité
        prioriteCombo.valueProperty().addListener((obs, old, val) -> {
            if (val == null) return;
            String color = switch (val) {
                case "HAUTE" -> "#EF4444";
                case "MOYENNE" -> "#F59E0B";
                default -> "#10B981";
            };
            prioriteCombo.setStyle(
                    "-fx-background-radius:10;-fx-border-radius:10;" +
                            "-fx-border-color:" + color + ";-fx-border-width:2;" +
                            "-fx-font-size:13px;"
            );
        });
    }

    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
    }

    public void setOnSuccess(Runnable r) {
        this.onSuccess = r;
    }

    @FXML
    public void creer() {
        // Validation
        String titre = titreField.getText() == null
                ? "" : titreField.getText().trim();
        String desc = descriptionArea.getText() == null
                ? "" : descriptionArea.getText().trim();
        String pStr = prioriteCombo.getValue();

        // Reset styles
        titreField.setStyle(
                "-fx-background-color:#F9FAFB;" +
                        "-fx-border-color:#E5E7EB;" +
                        "-fx-border-radius:10;-fx-background-radius:10;" +
                        "-fx-padding:12 14;-fx-font-size:13px;"
        );

        if (titre.isEmpty()) {
            showError("Le titre est obligatoire.");
            titreField.setStyle(
                    "-fx-background-color:#FFF5F5;" +
                            "-fx-border-color:#EF4444;-fx-border-width:2;" +
                            "-fx-border-radius:10;-fx-background-radius:10;" +
                            "-fx-padding:12 14;-fx-font-size:13px;"
            );
            titreField.requestFocus();
            return;
        }

        if (pStr == null) {
            showError("Choisissez une priorité.");
            return;
        }

        if (utilisateur == null) {
            showError("Utilisateur non connecté.");
            return;
        }

        Priorite priorite = Priorite.valueOf(pStr);
        int idCat = categorieCombo
                .getSelectionModel().getSelectedIndex() + 1;
        if (idCat <= 0) idCat = 1;

        boolean ok = ts.creerTache(
                titre,
                desc,
                priorite,
                datePicker.getValue(),
                utilisateur.getId(),
                idCat
        );

        if (ok) {
            if (onSuccess != null) onSuccess.run();
            fermer();
        } else {
            showError("Erreur lors de la création. Réessayez.");
        }
    }

    @FXML
    public void fermer() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        messageLabel.setStyle(
                "-fx-text-fill:#DC2626;" +
                        "-fx-font-size:12px;-fx-font-weight:700;"
        );
        messageLabel.setText("⚠ " + msg);
    }
}