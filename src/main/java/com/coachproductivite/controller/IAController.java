package com.coachproductivite.controller;

import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.service.IAService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

public class IAController implements UtilisateurAware {

    @FXML private TextArea promptField;
    @FXML private Label loadingLabel;
    @FXML private VBox chatBox;
    @FXML private VBox welcomeBox;
    @FXML private ScrollPane chatScroll;

    private Utilisateur utilisateur;
    private final IAService iaService = new IAService();

    @Override
    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
    }

    @FXML
    public void envoyerPrompt() {
        String prompt = promptField.getText() == null ? "" : promptField.getText().trim();
        if (prompt.isEmpty()) return;

        // Cache le message de bienvenue
        if (welcomeBox != null && welcomeBox.isVisible()) {
            welcomeBox.setVisible(false);
            welcomeBox.setManaged(false);
        }

        // Affiche le message de l'utilisateur
        ajouterMessageUtilisateur(prompt);
        promptField.clear();

        // Indicateur de chargement
        loadingLabel.setText("L'IA réfléchit...");
        VBox typingBubble = ajouterBulleTyping();

        new Thread(() -> {
            String reponse = iaService.decomposerTache(prompt);
            Platform.runLater(() -> {
                loadingLabel.setText("");
                chatBox.getChildren().remove(typingBubble);
                ajouterMessageIA(reponse != null ? reponse : "Désolé, je n'ai pas pu répondre.");
                scrollEnBas();
            });
        }).start();
    }

    private void ajouterMessageUtilisateur(String texte) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_RIGHT);

        Label message = new Label(texte);
        message.setWrapText(true);
        message.setMaxWidth(500);
        message.setStyle("-fx-background-color:linear-gradient(to bottom right,#6366F1,#8B5CF6);" +
                "-fx-text-fill:white;" +
                "-fx-padding:12 16;" +
                "-fx-background-radius:18 18 4 18;" +
                "-fx-font-size:13px;");

        container.getChildren().add(message);
        chatBox.getChildren().add(container);
        scrollEnBas();
    }

    private void ajouterMessageIA(String texte) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.TOP_LEFT);

        // Avatar
        StackPane avatar = new StackPane();
        Label bg = new Label();
        bg.setStyle("-fx-background-color:linear-gradient(to bottom right,#6366F1,#8B5CF6);" +
                "-fx-min-width:36px;-fx-min-height:36px;" +
                "-fx-max-width:36px;-fx-max-height:36px;" +
                "-fx-background-radius:50;");
        Label icon = new Label("✨");
        icon.setStyle("-fx-font-size:16px;");
        avatar.getChildren().addAll(bg, icon);

        // Bulle
        Label message = new Label(texte);
        message.setWrapText(true);
        message.setMaxWidth(560);
        message.setStyle("-fx-background-color:white;" +
                "-fx-text-fill:#111827;" +
                "-fx-padding:14 18;" +
                "-fx-background-radius:18 18 18 4;" +
                "-fx-border-color:#E5E7EB;" +
                "-fx-border-radius:18 18 18 4;" +
                "-fx-font-size:13px;" +
                "-fx-line-spacing:3;");

        container.getChildren().addAll(avatar, message);
        HBox.setHgrow(message, Priority.ALWAYS);
        chatBox.getChildren().add(container);
        scrollEnBas();
    }

    private VBox ajouterBulleTyping() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.TOP_LEFT);

        StackPane avatar = new StackPane();
        Label bg = new Label();
        bg.setStyle("-fx-background-color:linear-gradient(to bottom right,#6366F1,#8B5CF6);" +
                "-fx-min-width:36px;-fx-min-height:36px;" +
                "-fx-max-width:36px;-fx-max-height:36px;" +
                "-fx-background-radius:50;");
        Label icon = new Label("✨");
        icon.setStyle("-fx-font-size:16px;");
        avatar.getChildren().addAll(bg, icon);

        Label typing = new Label("● ● ●");
        typing.setStyle("-fx-background-color:white;" +
                "-fx-text-fill:#9CA3AF;" +
                "-fx-padding:14 18;" +
                "-fx-background-radius:18 18 18 4;" +
                "-fx-border-color:#E5E7EB;" +
                "-fx-border-radius:18 18 18 4;" +
                "-fx-font-size:14px;" +
                "-fx-letter-spacing:4;");

        container.getChildren().addAll(avatar, typing);

        VBox wrapper = new VBox(container);
        chatBox.getChildren().add(wrapper);
        scrollEnBas();
        return wrapper;
    }

    private void scrollEnBas() {
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    @FXML
    public void effacerChat() {
        chatBox.getChildren().clear();
        chatBox.getChildren().add(welcomeBox);
        welcomeBox.setVisible(true);
        welcomeBox.setManaged(true);
    }

    @FXML
    public void decomposer() {
        promptField.setText("Décompose cette tâche en sous-tâches détaillées sur 7 jours : ");
        promptField.requestFocus();
        promptField.positionCaret(promptField.getText().length());
    }

    @FXML
    public void suggererPriorites() {
        promptField.setText("Voici mes tâches. Propose un ordre de priorité avec une explication : ");
        promptField.requestFocus();
        promptField.positionCaret(promptField.getText().length());
    }

    @FXML
    public void reformuler() {
        promptField.setText("Reformule cet objectif de façon claire, motivante et SMART : ");
        promptField.requestFocus();
        promptField.positionCaret(promptField.getText().length());
    }

    @FXML
    public void planifier() {
        promptField.setText("Crée un plan de travail détaillé pour cette semaine : ");
        promptField.requestFocus();
        promptField.positionCaret(promptField.getText().length());
    }
}