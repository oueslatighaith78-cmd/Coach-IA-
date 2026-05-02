package com.coachproductivite.controller;

import com.coachproductivite.model.Tache;
import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.model.enums.EtatTache;
import com.coachproductivite.service.StatistiquesService;
import com.coachproductivite.service.TacheService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardController implements UtilisateurAware {

    @FXML private Label dateLabel;
    @FXML private Label totalLabel;
    @FXML private Label totalSubLabel;
    @FXML private Label termineesLabel;
    @FXML private Label termSubLabel;
    @FXML private Label tauxLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label conseilLabel;
    @FXML private Label nbLabel;
    @FXML private VBox tachesBox;

    private final TacheService ts = new TacheService();
    private final StatistiquesService ss = new StatistiquesService(); // si tu l'utilises plus tard

    @Override
    public void setUtilisateur(Utilisateur u) {
        if (u == null) return;
        charger(u);
    }

    private void charger(Utilisateur u) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        dateLabel.setText(LocalDate.now().format(f));

        List<Tache> taches = ts.getTaches(u.getId());

        long total = taches.size();
        long done = taches.stream().filter(t -> t.getEtat() == EtatTache.TERMINEE).count();
        long enc  = taches.stream().filter(t -> t.getEtat() == EtatTache.EN_COURS).count();
        long af   = taches.stream().filter(t -> t.getEtat() == EtatTache.A_FAIRE).count();

        double taux = total > 0 ? (double) done / total * 100 : 0;

        totalLabel.setText(String.valueOf(total));
        totalSubLabel.setText(af + " à faire");
        termineesLabel.setText(String.valueOf(done));
        termSubLabel.setText("sur " + total);
        tauxLabel.setText(String.format("%.0f%%", taux));
        enCoursLabel.setText(String.valueOf(enc));

        long haute = taches.stream()
                .filter(t -> t.getEtat() != EtatTache.TERMINEE && "HAUTE".equals(t.getPriorite().name()))
                .count();

        String conseil;
        if (taches.isEmpty()) {
            conseil = "Bienvenue ! Commencez par créer votre première tâche dans 'Mes tâches'.";
        } else if (haute > 0) {
            conseil = "Vous avez " + haute + " tâche(s) haute priorité en attente. Concentrez-vous sur celles-ci.";
        } else if (taux >= 80) {
            conseil = "Excellent ! " + String.format("%.0f%%", taux) + " de complétion. Continuez comme ça !";
        } else {
            conseil = "Progression à " + String.format("%.0f%%", taux) + ". Utilisez l'Assistant IA pour décomposer vos tâches.";
        }
        conseilLabel.setText(conseil);

        nbLabel.setText(total + " tâche(s)");
        tachesBox.getChildren().clear();

        if (taches.isEmpty()) {
            Label v = new Label("Aucune tâche — créez-en une !");
            v.setStyle("-fx-text-fill:#9CA3AF;-fx-font-size:13px;-fx-padding:16 0;");
            tachesBox.getChildren().add(v);
        } else {
            taches.stream().limit(6).forEach(t -> tachesBox.getChildren().add(row(t)));
        }
    }

    private HBox row(Tache t) {
        HBox r = new HBox(14);
        r.getStyleClass().add("task-row");
        r.setAlignment(Pos.CENTER_LEFT);

        Label bar = new Label();
        bar.setMinSize(5, 46);
        bar.setMaxWidth(5);
        bar.setStyle("-fx-background-color:" + col(t.getPriorite().name()) + ";-fx-background-radius:3;");

        Label chk = new Label(t.getEtat() == EtatTache.TERMINEE ? "✓" : "○");
        chk.setStyle("-fx-font-size:16px;-fx-text-fill:" + (t.getEtat()==EtatTache.TERMINEE ? "#059669;" : "#D1D5DB;"));

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        boolean done = t.getEtat() == EtatTache.TERMINEE;
        Label titre = new Label(t.getTitre());
        titre.setStyle("-fx-font-size:13px;-fx-font-weight:bold;" +
                "-fx-text-fill:" + (done ? "#9CA3AF;" : "#111827;") +
                (done ? "-fx-strikethrough:true;" : ""));

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

        badges.getChildren().addAll(
                badge(t.getPriorite().name(), prioBg(t.getPriorite().name()), prioFg(t.getPriorite().name())),
                badge(t.getEtat().name().replace("_"," "), etatBg(t.getEtat()), etatFg(t.getEtat()))
        );

        if (t.getEcheance() != null) {
            Label ech = new Label("📅 " + t.getEcheance());
            ech.setStyle("-fx-font-size:11px;-fx-text-fill:#9CA3AF;");
            badges.getChildren().add(ech);
        }

        info.getChildren().addAll(titre, badges);
        r.getChildren().addAll(bar, chk, info);
        return r;
    }

    private Label badge(String txt, String bg, String fg) {
        Label l = new Label(txt);
        l.setStyle("-fx-background-color:"+bg+";" +
                "-fx-text-fill:"+fg+";" +
                "-fx-font-size:10px;-fx-font-weight:bold;" +
                "-fx-padding:3 10;-fx-background-radius:20;");
        return l;
    }

    private String col(String p) {
        return switch (p) {
            case "HAUTE" -> "#EF4444";
            case "MOYENNE" -> "#F59E0B";
            default -> "#10B981";
        };
    }

    private String prioBg(String p) {
        return switch (p) {
            case "HAUTE" -> "#FEE2E2";
            case "MOYENNE" -> "#FEF3C7";
            default -> "#D1FAE5";
        };
    }

    private String prioFg(String p) {
        return switch (p) {
            case "HAUTE" -> "#991B1B";
            case "MOYENNE" -> "#92400E";
            default -> "#065F46";
        };
    }

    private String etatBg(EtatTache e) {
        return switch (e) {
            case TERMINEE -> "#D1FAE5";
            case EN_COURS -> "#E0E7FF";
            case ANNULEE -> "#FEE2E2";
            default -> "#F3F4F6";
        };
    }

    private String etatFg(EtatTache e) {
        return switch (e) {
            case TERMINEE -> "#065F46";
            case EN_COURS -> "#3730A3";
            case ANNULEE -> "#991B1B";
            default -> "#6B7280";
        };
    }
}