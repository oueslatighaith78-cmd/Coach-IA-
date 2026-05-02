package com.coachproductivite.controller;

import com.coachproductivite.model.Tache;
import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.model.enums.EtatTache;
import com.coachproductivite.service.TacheService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CalendrierController implements UtilisateurAware {

    @FXML private Label moisLabel;
    @FXML private GridPane calGrid;
    @FXML private Label jourLabel;
    @FXML private Label compteurJourLabel;
    @FXML private VBox tachesJourBox;

    private Utilisateur utilisateur;
    private final TacheService ts = new TacheService();
    private YearMonth mois = YearMonth.now();
    private List<Tache> taches;
    private LocalDate jourSelectionne = null;

    private static final DateTimeFormatter FMT_MOIS =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter FMT_JOUR =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

    @Override
    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        taches = ts.getTaches(u.getId());
        afficher();
        allerAujourdhui();
    }

    @FXML
    public void allerAujourdhui() {
        mois = YearMonth.now();
        afficher();
        afficherJour(LocalDate.now());
    }

    private void afficher() {
        moisLabel.setText(mois.format(FMT_MOIS).toUpperCase(Locale.FRENCH));
        calGrid.getChildren().removeIf(n -> GridPane.getRowIndex(n) != null);

        LocalDate premier = mois.atDay(1);
        int decalage = premier.getDayOfWeek().getValue() - 1; // 0 = lundi
        int nbJours = mois.lengthOfMonth();
        int col = decalage, row = 0;

        for (int j = 1; j <= nbJours; j++) {
            LocalDate d = mois.atDay(j);
            VBox cell = creerCelluleJour(d, j);
            calGrid.add(cell, col, row);
            col++;
            if (col > 6) { col = 0; row++; }
        }
    }

    private VBox creerCelluleJour(LocalDate d, int num) {
        VBox cell = new VBox(4);
        cell.setAlignment(Pos.CENTER);
        cell.setMinSize(52, 52);
        cell.setMaxSize(52, 52);

        boolean isToday = d.equals(LocalDate.now());
        boolean isSelected = d.equals(jourSelectionne);

        // Détermine la couleur dominante du jour selon les tâches
        String couleur = couleurJour(d); // "urgent", "encours", "terminee", "avenir", "vide"

        // Style de fond
        String bg, border, textFill;

        if (isSelected) {
            bg = "#4F46E5";
            border = "#4F46E5";
            textFill = "white";
        } else if (isToday) {
            bg = "#EEF2FF";
            border = "#4F46E5";
            textFill = "#4F46E5";
        } else {
            switch (couleur) {
                case "urgent" -> { bg = "#FEF2F2"; border = "#FECACA"; textFill = "#DC2626"; }
                case "encours" -> { bg = "#FFFBEB"; border = "#FDE68A"; textFill = "#D97706"; }
                case "terminee" -> { bg = "#ECFDF5"; border = "#A7F3D0"; textFill = "#059669"; }
                case "avenir" -> { bg = "#EEF2FF"; border = "#C7D2FE"; textFill = "#4F46E5"; }
                default -> { bg = "white"; border = "#F3F4F6"; textFill = "#374151"; }
            }
        }

        cell.setStyle("-fx-background-color:" + bg + ";" +
                "-fx-border-color:" + border + ";" +
                "-fx-border-width:2;" +
                "-fx-background-radius:12;" +
                "-fx-border-radius:12;" +
                "-fx-cursor:hand;");

        // Numéro du jour
        Label numLabel = new Label(String.valueOf(num));
        numLabel.setStyle("-fx-font-size:14px;-fx-font-weight:800;-fx-text-fill:" + textFill + ";");

        // Dots des tâches (petits cercles colorés sous le numéro)
        HBox dots = new HBox(3);
        dots.setAlignment(Pos.CENTER);
        List<String> couleursDots = couleursTachesJour(d);
        for (String c : couleursDots.stream().distinct().limit(3).toList()) {
            Circle dot = new Circle(3);
            dot.setFill(Color.web(c));
            dots.getChildren().add(dot);
        }

        cell.getChildren().addAll(numLabel, dots);
        cell.setOnMouseClicked(e -> afficherJour(d));

        return cell;
    }

    /**
     * Détermine la catégorie visuelle dominante d'un jour
     * Priorité : urgent > en cours > terminée > à venir
     */
    private String couleurJour(LocalDate d) {
        List<Tache> jourTaches = taches.stream()
                .filter(t -> d.equals(t.getEcheance()))
                .toList();

        if (jourTaches.isEmpty()) return "vide";

        boolean urgent = jourTaches.stream()
                .anyMatch(t -> t.getEtat() != EtatTache.TERMINEE
                        && ChronoUnit.DAYS.between(LocalDate.now(), d) <= 2
                        && !d.isBefore(LocalDate.now()));

        boolean encours = jourTaches.stream()
                .anyMatch(t -> t.getEtat() == EtatTache.EN_COURS);

        boolean terminee = jourTaches.stream()
                .anyMatch(t -> t.getEtat() == EtatTache.TERMINEE);

        boolean avenir = jourTaches.stream()
                .anyMatch(t -> t.getEtat() == EtatTache.A_FAIRE);

        if (urgent) return "urgent";
        if (encours) return "encours";
        if (terminee) return "terminee";
        if (avenir) return "avenir";
        return "vide";
    }

    private List<String> couleursTachesJour(LocalDate d) {
        return taches.stream()
                .filter(t -> d.equals(t.getEcheance()))
                .map(t -> {
                    if (t.getEtat() == EtatTache.TERMINEE) return "#10B981";
                    if (ChronoUnit.DAYS.between(LocalDate.now(), d) <= 2 && !d.isBefore(LocalDate.now()))
                        return "#EF4444";
                    if (t.getEtat() == EtatTache.EN_COURS) return "#F59E0B";
                    return "#4F46E5";
                })
                .toList();
    }

    private void afficherJour(LocalDate d) {
        this.jourSelectionne = d;
        afficher(); // refresh pour montrer la sélection

        jourLabel.setText(d.format(FMT_JOUR));
        tachesJourBox.getChildren().clear();

        List<Tache> dj = taches.stream()
                .filter(t -> d.equals(t.getEcheance()))
                .collect(Collectors.toList());

        compteurJourLabel.setText(dj.size() + " tâche(s)");

        if (dj.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-padding:40 0;");
            Label icon = new Label("📅");
            icon.setStyle("-fx-font-size:32px;");
            Label txt = new Label("Aucune tâche ce jour");
            txt.setStyle("-fx-font-size:14px;-fx-text-fill:#9CA3AF;");
            empty.getChildren().addAll(icon, txt);
            tachesJourBox.getChildren().add(empty);
            return;
        }

        for (Tache t : dj) {
            tachesJourBox.getChildren().add(creerCarteTache(t));
        }
    }

    private HBox creerCarteTache(Tache t) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:#F9FAFB;" +
                "-fx-background-radius:12;" +
                "-fx-border-radius:12;" +
                "-fx-border-color:#F3F4F6;" +
                "-fx-border-width:1;" +
                "-fx-padding:14 16;");

        // Barre latérale colorée selon priorité
        Label bar = new Label();
        bar.setMinSize(4, 40);
        bar.setMaxWidth(4);
        bar.setStyle("-fx-background-color:" + pCol(t.getPriorite().name()) +
                ";-fx-background-radius:3;");

        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titre = new Label(t.getTitre());
        titre.setStyle("-fx-font-weight:800;-fx-font-size:14px;-fx-text-fill:#111827;");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

        // Badge état avec couleur dynamique
        String etatTexte = t.getEtat().name().replace("_", " ");
        String etatCouleur = etatCouleur(t.getEtat());
        Label badgeEtat = new Label(etatTexte);
        badgeEtat.setStyle("-fx-background-color:" + etatCouleur + "20;" +
                "-fx-text-fill:" + etatCouleur + ";" +
                "-fx-font-size:10px;-fx-font-weight:800;" +
                "-fx-padding:4 10;-fx-background-radius:20;");

        // Badge priorité
        Label badgePrio = new Label(t.getPriorite().name());
        badgePrio.setStyle("-fx-background-color:" + pBg(t.getPriorite().name()) + ";" +
                "-fx-text-fill:" + pFg(t.getPriorite().name()) + ";" +
                "-fx-font-size:10px;-fx-font-weight:800;" +
                "-fx-padding:4 10;-fx-background-radius:20;");

        badges.getChildren().addAll(badgeEtat, badgePrio);

        // Description si présente
        if (t.getDescription() != null && !t.getDescription().isBlank()) {
            Label desc = new Label(t.getDescription());
            desc.setWrapText(true);
            desc.setStyle("-fx-font-size:12px;-fx-text-fill:#6B7280;");
            info.getChildren().addAll(titre, badges, desc);
        } else {
            info.getChildren().addAll(titre, badges);
        }

        card.getChildren().addAll(bar, info);
        return card;
    }

    private String etatCouleur(EtatTache e) {
        return switch (e) {
            case TERMINEE -> "#059669";
            case EN_COURS -> "#D97706";
            case A_FAIRE -> "#4F46E5";
            case ANNULEE -> "#DC2626";
        };
    }

    private String pCol(String p) {
        return switch (p) {
            case "HAUTE" -> "#EF4444";
            case "MOYENNE" -> "#F59E0B";
            default -> "#10B981";
        };
    }

    private String pBg(String p) {
        return switch (p) {
            case "HAUTE" -> "#FEE2E2";
            case "MOYENNE" -> "#FEF3C7";
            default -> "#D1FAE5";
        };
    }

    private String pFg(String p) {
        return switch (p) {
            case "HAUTE" -> "#991B1B";
            case "MOYENNE" -> "#92400E";
            default -> "#065F46";
        };
    }

    @FXML
    public void moisPrecedent() {
        mois = mois.minusMonths(1);
        afficher();
    }

    @FXML
    public void moisSuivant() {
        mois = mois.plusMonths(1);
        afficher();
    }
}