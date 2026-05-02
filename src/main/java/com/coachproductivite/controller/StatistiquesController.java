package com.coachproductivite.controller;

import com.coachproductivite.model.Session;
import com.coachproductivite.model.Statistiques;
import com.coachproductivite.model.Tache;
import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.model.enums.EtatTache;
import com.coachproductivite.service.SessionService;
import com.coachproductivite.service.StatistiquesService;
import com.coachproductivite.service.TacheService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatistiquesController implements UtilisateurAware {

    @FXML private Label termineesSemaineLabel;
    @FXML private Label tempsTotalLabel;
    @FXML private Label streakLabel;
    @FXML private Label tauxLabel;
    @FXML private Label tauxDetailLabel;
    @FXML private Label resumeLabel;

    @FXML private HBox alerteRetard;
    @FXML private Label alerteRetardTitre;
    @FXML private Label alerteRetardDetail;

    @FXML private BarChart<String, Number> evolutionChart;
    @FXML private PieChart categorieChart;
    @FXML private VBox topTachesBox;

    private Utilisateur utilisateur;
    private final StatistiquesService statsService = new StatistiquesService();
    private final TacheService tacheService = new TacheService();
    private final SessionService sessionService = new SessionService();

    private static final String[] CATS = {"Études", "Travail", "Personnel"};

    @Override
    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        calculerStats();
    }

    @FXML
    public void calculerStats() {
        if (utilisateur == null) return;

        Statistiques stats = statsService.calculerStats(utilisateur.getId());
        List<Tache> taches = tacheService.getTaches(utilisateur.getId());

        // === KPI ===
        afficherKPI(stats, taches);

        // === ALERTE RETARD ===
        afficherAlerteRetard(taches);

        // === GRAPHIQUE 7 JOURS ===
        afficherEvolution7Jours(taches);

        // === TOP 5 TÂCHES PAR TEMPS ===
        afficherTopTaches(taches);

        // === RÉPARTITION CATÉGORIES ===
        afficherCategories(taches);

        // === RÉSUMÉ ===
        resumeLabel.setText(genererBilan(stats, taches));
    }

    private void afficherKPI(Statistiques stats, List<Tache> taches) {
        // Taux global
        tauxLabel.setText(String.format("%.0f%%", stats.getTauxCompletion()));
        tauxDetailLabel.setText(stats.getTachesTerminees() + "/" + stats.getTachesTotal() + " tâches");

        // Tâches terminées cette semaine (approximation : on prend toutes les terminées)
        // NOTE : pour être précis, il faudrait une date_termine en BDD
        long termineesSemaine = taches.stream()
                .filter(t -> t.getEtat() == EtatTache.TERMINEE)
                .count();
        termineesSemaineLabel.setText(String.valueOf(termineesSemaine));

        // Temps total (somme de toutes les sessions de toutes les tâches)
        int tempsTotalMin = 0;
        for (Tache t : taches) {
            tempsTotalMin += sessionService.getTempsTotalParTache(t.getId());
        }
        tempsTotalLabel.setText(formaterDuree(tempsTotalMin));

        // Streak (calcul approximatif basé sur les sessions)
        int streak = calculerStreak(taches);
        streakLabel.setText(String.valueOf(streak));
    }

    private void afficherAlerteRetard(List<Tache> taches) {
        LocalDate today = LocalDate.now();
        List<Tache> retard = taches.stream()
                .filter(t -> t.getEtat() != EtatTache.TERMINEE)
                .filter(t -> t.getEcheance() != null && t.getEcheance().isBefore(today))
                .collect(Collectors.toList());

        if (retard.isEmpty()) {
            alerteRetard.setVisible(false);
            alerteRetard.setManaged(false);
        } else {
            alerteRetard.setVisible(true);
            alerteRetard.setManaged(true);
            alerteRetardTitre.setText(retard.size() + " tâche(s) en retard !");
            String premiereDate = retard.get(0).getEcheance()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            alerteRetardDetail.setText("La plus ancienne : \"" + retard.get(0).getTitre()
                    + "\" était due le " + premiereDate);
        }
    }

    private void afficherEvolution7Jours(List<Tache> taches) {
        // Construit les données pour les 7 derniers jours
        // Comme on n'a pas de date_termine, on utilise les sessions comme indicateur d'activité
        Map<LocalDate, Integer> sessionsParJour = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            sessionsParJour.put(today.minusDays(i), 0);
        }

        for (Tache t : taches) {
            List<Session> sessions = sessionService.getSessions(t.getId());
            for (Session s : sessions) {
                if (s.getDateSession() != null) {
                    LocalDate jour = s.getDateSession().toLocalDate();
                    if (sessionsParJour.containsKey(jour)) {
                        sessionsParJour.put(jour, sessionsParJour.get(jour) + 1);
                    }
                }
            }
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE", Locale.FRENCH);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        List<XYChart.Data<String, Number>> dataList = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> e : sessionsParJour.entrySet()) {
            String label = e.getKey().format(fmt);
            XYChart.Data<String, Number> data = new XYChart.Data<>(label, e.getValue());
            dataList.add(data);
            series.getData().add(data);
        }

        evolutionChart.getData().clear();
        evolutionChart.setData(FXCollections.observableArrayList(series));

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : dataList) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-bar-fill:linear-gradient(to top,#6366F1,#8B5CF6);");
                }
            }
        });
    }

    private void afficherTopTaches(List<Tache> taches) {
        // Calcule le temps par tâche
        Map<Tache, Integer> tempsParTache = new HashMap<>();
        for (Tache t : taches) {
            int temps = sessionService.getTempsTotalParTache(t.getId());
            if (temps > 0) {
                tempsParTache.put(t, temps);
            }
        }

        // Top 5
        List<Map.Entry<Tache, Integer>> top5 = tempsParTache.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        topTachesBox.getChildren().clear();

        if (top5.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-padding:30 0;");
            Label icon = new Label("⏱");
            icon.setStyle("-fx-font-size:32px;-fx-opacity:0.4;");
            Label txt = new Label("Aucune session enregistrée");
            txt.setStyle("-fx-text-fill:#9CA3AF;-fx-font-size:12px;");
            Label hint = new Label("Utilise le chronomètre pour suivre ton temps");
            hint.setStyle("-fx-text-fill:#D1D5DB;-fx-font-size:11px;");
            empty.getChildren().addAll(icon, txt, hint);
            topTachesBox.getChildren().add(empty);
            return;
        }

        int maxTemps = top5.get(0).getValue();
        int rang = 1;
        for (Map.Entry<Tache, Integer> e : top5) {
            topTachesBox.getChildren().add(creerLigneTopTache(rang++, e.getKey(), e.getValue(), maxTemps));
        }
    }

    private HBox creerLigneTopTache(int rang, Tache t, int minutes, int maxMin) {
        HBox ligne = new HBox(12);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setStyle("-fx-padding:6 0;");

        // Médaille
        String medaille = switch (rang) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "  " + rang;
        };
        Label rangLabel = new Label(medaille);
        rangLabel.setStyle("-fx-font-size:16px;-fx-min-width:30px;");

        // Info
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titre = new Label(t.getTitre());
        titre.setStyle("-fx-font-weight:700;-fx-font-size:13px;-fx-text-fill:#111827;");

        ProgressBar bar = new ProgressBar();
        bar.setProgress((double) minutes / maxMin);
        bar.setPrefHeight(6);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent:linear-gradient(to right,#6366F1,#8B5CF6);");

        info.getChildren().addAll(titre, bar);

        // Temps
        Label tempsLabel = new Label(formaterDuree(minutes));
        tempsLabel.setStyle("-fx-font-weight:800;-fx-font-size:13px;-fx-text-fill:#4F46E5;-fx-min-width:60px;");

        ligne.getChildren().addAll(rangLabel, info, tempsLabel);
        return ligne;
    }

    private void afficherCategories(List<Tache> taches) {
        Map<String, Long> compteurs = new LinkedHashMap<>();
        compteurs.put("Études", 0L);
        compteurs.put("Travail", 0L);
        compteurs.put("Personnel", 0L);

        for (Tache t : taches) {
            int idCat = t.getIdCategorie();
            if (idCat >= 1 && idCat <= CATS.length) {
                String nom = CATS[idCat - 1];
                compteurs.put(nom, compteurs.get(nom) + 1);
            }
        }

        List<PieChart.Data> dataList = new ArrayList<>();
        for (Map.Entry<String, Long> e : compteurs.entrySet()) {
            if (e.getValue() > 0) {
                dataList.add(new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()));
            }
        }

        if (dataList.isEmpty()) {
            categorieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Aucune tâche", 1)));
        } else {
            categorieChart.setData(FXCollections.observableArrayList(dataList));
        }

        // Force les couleurs APRÈS rendu (Platform.runLater)
        Platform.runLater(() -> {
            // Couleurs du cercle (slices)
            for (PieChart.Data d : categorieChart.getData()) {
                Node node = d.getNode();
                if (node != null) {
                    String nom = d.getName();
                    String couleur;
                    if (nom.startsWith("Études")) couleur = "#6366F1";
                    else if (nom.startsWith("Travail")) couleur = "#10B981";
                    else if (nom.startsWith("Personnel")) couleur = "#F59E0B";
                    else couleur = "#94A3B8";
                    node.setStyle("-fx-pie-color:" + couleur + ";");
                }
            }

            // Couleurs des dots dans la légende
            Set<Node> legendItems = categorieChart.lookupAll(".chart-legend-item-symbol");
            int i = 0;
            for (Node item : legendItems) {
                if (i >= categorieChart.getData().size()) break;
                String nom = categorieChart.getData().get(i).getName();
                String couleur;
                if (nom.startsWith("Études")) couleur = "#6366F1";
                else if (nom.startsWith("Travail")) couleur = "#10B981";
                else if (nom.startsWith("Personnel")) couleur = "#F59E0B";
                else couleur = "#94A3B8";
                item.setStyle("-fx-background-color:" + couleur + ";");
                i++;
            }
        });
    }

    private int calculerStreak(List<Tache> taches) {
        Set<LocalDate> joursActifs = new HashSet<>();
        for (Tache t : taches) {
            for (Session s : sessionService.getSessions(t.getId())) {
                if (s.getDateSession() != null) {
                    joursActifs.add(s.getDateSession().toLocalDate());
                }
            }
        }

        if (joursActifs.isEmpty()) return 0;

        int streak = 0;
        LocalDate jour = LocalDate.now();
        while (joursActifs.contains(jour)) {
            streak++;
            jour = jour.minusDays(1);
        }
        return streak;
    }

    private String formaterDuree(int minutes) {
        if (minutes < 60) return minutes + "min";
        int h = minutes / 60;
        int m = minutes % 60;
        return m == 0 ? h + "h" : h + "h" + m;
    }

    private String genererBilan(Statistiques stats, List<Tache> taches) {
        if (stats.getTachesTotal() == 0) {
            return "🌟 Bienvenue ! Commence par créer ta première tâche dans 'Mes tâches' pour suivre ta productivité.";
        }

        double taux = stats.getTauxCompletion();
        int tempsTotal = 0;
        for (Tache t : taches) {
            tempsTotal += sessionService.getTempsTotalParTache(t.getId());
        }

        StringBuilder sb = new StringBuilder();

        // Performance
        if (taux >= 80) {
            sb.append("🎉 Bravo ! Avec ").append(String.format("%.0f%%", taux))
                    .append(" de complétion, tu es très productif. ");
        } else if (taux >= 50) {
            sb.append("💪 Bon rythme avec ").append(String.format("%.0f%%", taux))
                    .append(" de complétion. ");
        } else if (taux >= 25) {
            sb.append("⚡ Tu es à ").append(String.format("%.0f%%", taux))
                    .append(". Concentre-toi sur les priorités hautes. ");
        } else {
            sb.append("🚀 Démarrage en cours. ");
        }

        // Temps
        if (tempsTotal > 0) {
            sb.append("Tu as enregistré ").append(formaterDuree(tempsTotal))
                    .append(" de travail au total. ");
        } else {
            sb.append("\n\n💡 Astuce : utilise le chronomètre pour suivre ton temps réel sur chaque tâche.");
        }

        return sb.toString();
    }
}