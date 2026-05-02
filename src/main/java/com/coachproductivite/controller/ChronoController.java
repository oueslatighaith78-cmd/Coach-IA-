package com.coachproductivite.controller;

import com.coachproductivite.model.Tache;
import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.model.enums.EtatTache;
import com.coachproductivite.service.SessionService;
import com.coachproductivite.service.TacheService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class ChronoController implements UtilisateurAware {

    @FXML private Label chronoLabel;
    @FXML private ComboBox<Tache> tacheChronoCombo;
    @FXML private Button btnStartStop;
    @FXML private Label chronoMessageLabel;

    private final TacheService tacheService = new TacheService();
    private final SessionService sessionService = new SessionService();

    private Timeline timeline;

    /* ── État persistant entre les pages ── */
    private static int secondes = 0;
    private static boolean running = false;
    private static Tache tacheEnCours = null;

    @Override
    public void setUtilisateur(Utilisateur u) {
        chargerTaches(u.getId());

        if (tacheEnCours != null) {
            for (Tache t : tacheChronoCombo.getItems()) {
                if (t.getId() == tacheEnCours.getId()) {
                    tacheChronoCombo.setValue(t);
                    break;
                }
            }
        }

        majAffichage();

        if (running) {
            btnStartStop.setText("⏸  Pause");
            btnStartStop.setStyle("-fx-background-color:#EF4444;" +
                    "-fx-text-fill:white;-fx-font-weight:800;" +
                    "-fx-font-size:13px;-fx-background-radius:12;" +
                    "-fx-cursor:hand;");
            demarrerTimeline();
            tacheChronoCombo.setDisable(true);
        } else {
            btnStartStop.setText(secondes > 0 ? "▶  Reprendre" : "▶  Démarrer");
            btnStartStop.setStyle("-fx-background-color:#4F46E5;" +
                    "-fx-text-fill:white;-fx-font-weight:800;" +
                    "-fx-font-size:13px;-fx-background-radius:12;" +
                    "-fx-cursor:hand;");
            tacheChronoCombo.setDisable(false);
        }
    }

    /**
     * Charge uniquement les tâches NON terminées et NON annulées
     */
    private void chargerTaches(int userId) {
        List<Tache> taches = tacheService.getTaches(userId);

        // Filtre : ne garder que les tâches actives (À faire ou En cours)
        List<Tache> tachesActives = taches.stream()
                .filter(t -> t.getEtat() != EtatTache.TERMINEE
                        && t.getEtat() != EtatTache.ANNULEE)
                .collect(Collectors.toList());

        tacheChronoCombo.setItems(FXCollections.observableArrayList(tachesActives));
        tacheChronoCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Tache t) {
                return t != null ? t.getTitre() : "";
            }
            @Override
            public Tache fromString(String s) {
                return null;
            }
        });

        // Message si aucune tâche active disponible
        if (tachesActives.isEmpty()) {
            tacheChronoCombo.setPromptText("Aucune tâche active disponible");
        } else {
            tacheChronoCombo.setPromptText("Sélectionner une tâche...");
        }
    }

    @FXML
    public void startStopChrono() {
        if (!running) {
            demarrer();
        } else {
            pause();
        }
    }

    private void demarrer() {
        if (tacheChronoCombo.getValue() == null) {
            msg("Sélectionnez une tâche avant de démarrer.", false);
            return;
        }

        tacheEnCours = tacheChronoCombo.getValue();
        running = true;
        tacheChronoCombo.setDisable(true);

        btnStartStop.setText("⏸  Pause");
        btnStartStop.setStyle("-fx-background-color:#EF4444;" +
                "-fx-text-fill:white;-fx-font-weight:800;" +
                "-fx-font-size:13px;-fx-background-radius:12;" +
                "-fx-cursor:hand;");
        msg("", true);

        demarrerTimeline();
    }

    private void demarrerTimeline() {
        if (timeline != null) timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondes++;
            majAffichage();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void pause() {
        if (timeline != null) timeline.stop();
        running = false;

        btnStartStop.setText("▶  Reprendre");
        btnStartStop.setStyle("-fx-background-color:#4F46E5;" +
                "-fx-text-fill:white;-fx-font-weight:800;" +
                "-fx-font-size:13px;-fx-background-radius:12;" +
                "-fx-cursor:hand;");
    }

    @FXML
    public void terminerChrono() {
        if (timeline != null) timeline.stop();
        running = false;

        Tache t = (tacheEnCours != null) ? tacheEnCours : tacheChronoCombo.getValue();

        if (t == null) {
            msg("Aucune tâche sélectionnée.", false);
            return;
        }

        int userId = t.getIdUtilisateur();

        if (secondes > 0) {
            int min = Math.max(1, secondes / 60);
            // 1. Enregistrer le temps
            sessionService.enregistrerSession(t.getId(), min);
            // 2. Marquer la tâche comme terminée
            tacheService.terminerTache(t.getId());
            msg("✓ Tâche '" + t.getTitre() + "' terminée ! " + min + " min enregistrées.", true);
        } else {
            // Même si chrono à 0, on termine la tâche
            tacheService.terminerTache(t.getId());
            msg("✓ Tâche '" + t.getTitre() + "' marquée comme terminée.", true);
        }

        resetEtatComplet();
        // Recharge la liste pour enlever la tâche qu'on vient de terminer
        chargerTaches(userId);
    }

    @FXML
    public void resetChrono() {
        if (timeline != null) timeline.stop();
        running = false;
        msg("", true);
        resetEtatComplet();
    }

    private void resetEtatComplet() {
        secondes = 0;
        tacheEnCours = null;
        majAffichage();

        btnStartStop.setText("▶  Démarrer");
        btnStartStop.setStyle("-fx-background-color:#4F46E5;" +
                "-fx-text-fill:white;-fx-font-weight:800;" +
                "-fx-font-size:13px;-fx-background-radius:12;" +
                "-fx-cursor:hand;");

        tacheChronoCombo.setDisable(false);
    }

    private void majAffichage() {
        int h = secondes / 3600;
        int m = (secondes % 3600) / 60;
        int s = secondes % 60;
        chronoLabel.setText(String.format("%02d : %02d : %02d", h, m, s));
    }

    private void msg(String text, boolean ok) {
        chronoMessageLabel.setStyle("-fx-text-fill:" + (ok ? "#059669" : "#DC2626") + ";");
        chronoMessageLabel.setText(text);
    }
}