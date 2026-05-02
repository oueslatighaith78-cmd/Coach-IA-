package com.coachproductivite.controller;

import com.coachproductivite.model.SousTache;
import com.coachproductivite.model.Tache;
import com.coachproductivite.model.Utilisateur;
import com.coachproductivite.model.enums.EtatTache;
import com.coachproductivite.service.IAService;
import com.coachproductivite.service.SousTacheService;
import com.coachproductivite.service.TacheService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TacheController implements UtilisateurAware {

    @FXML private Label nbTachesLabel;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtreEtatCombo;
    @FXML private ComboBox<String> filtrePrioriteCombo;
    @FXML private ComboBox<String> filtreCategorieCombo;

    @FXML private TableView<Tache> tachesTable;
    @FXML private TableColumn<Tache, String> titreCol;
    @FXML private TableColumn<Tache, String> prioriteCol;
    @FXML private TableColumn<Tache, String> etatCol;
    @FXML private TableColumn<Tache, String> echeanceCol;
    @FXML private TableColumn<Tache, String> categorieCol;

    @FXML private Label detailTitreLabel;
    @FXML private Label detailPrioriteLabel;
    @FXML private Label detailEtatLabel;
    @FXML private Label detailCategorieLabel;
    @FXML private Label detailEcheanceLabel;
    @FXML private Label nbSousTachesLabel;
    @FXML private Label hintSelectionLabel;

    @FXML private ListView<SousTache> sousTachesList;
    @FXML private TextField nouvelleSousTacheField;
    @FXML private Button btnTerminer;
    @FXML private Button btnSupprimer;
    @FXML private Button btnDecomposerIA;

    private Utilisateur utilisateur;
    private final TacheService ts = new TacheService();
    private final SousTacheService sts = new SousTacheService();
    private final IAService ias = new IAService();

    private static final String[] CATS = {"Études", "Travail", "Personnel"};
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObservableList<Tache> allTaches = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltres();
        configurerSelection();
        configurerSousTachesView();
        setDetailsEmpty();
    }

    @Override
    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        reload();
    }

    private void configurerColonnes() {
        titreCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitre()));

        prioriteCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriorite().name()));
        prioriteCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                setGraphic(badge(val, pBg(val), pFg(val)));
                setText(null);
            }
        });

        etatCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEtat().name()));
        etatCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                EtatTache e = EtatTache.valueOf(val);
                setGraphic(badge(val.replace("_", " "), eBg(e), eFg(e)));
                setText(null);
            }
        });

        echeanceCol.setCellValueFactory(d -> {
            LocalDate ech = d.getValue().getEcheance();
            return new SimpleStringProperty(ech != null ? ech.format(DF) : "—");
        });

        categorieCol.setCellValueFactory(d -> {
            int id = d.getValue().getIdCategorie();
            return new SimpleStringProperty((id >= 1 && id <= CATS.length) ? CATS[id - 1] : "—");
        });
        categorieCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null || "—".equals(val)) {
                    setText("—"); setGraphic(null); return;
                }
                String bg = switch (val) {
                    case "Études"  -> "#EEF2FF";
                    case "Travail" -> "#D1FAE5";
                    default        -> "#FEF3C7";
                };
                String fg = switch (val) {
                    case "Études"  -> "#3730A3";
                    case "Travail" -> "#065F46";
                    default        -> "#92400E";
                };
                setGraphic(badge(val, bg, fg));
                setText(null);
            }
        });

        tachesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurerFiltres() {
        filtreEtatCombo.setItems(FXCollections.observableArrayList(
                "TOUS", "A_FAIRE", "EN_COURS", "TERMINEE", "ANNULEE"));
        filtrePrioriteCombo.setItems(FXCollections.observableArrayList(
                "TOUS", "HAUTE", "MOYENNE", "FAIBLE"));

        ObservableList<String> cats = FXCollections.observableArrayList();
        cats.add("TOUS");
        cats.addAll(CATS);
        filtreCategorieCombo.setItems(cats);

        filtreEtatCombo.setValue("TOUS");
        filtrePrioriteCombo.setValue("TOUS");
        filtreCategorieCombo.setValue("TOUS");

        searchField.textProperty().addListener((o, ov, nv) -> appliquerFiltres());
        filtreEtatCombo.valueProperty().addListener((o, ov, nv) -> appliquerFiltres());
        filtrePrioriteCombo.valueProperty().addListener((o, ov, nv) -> appliquerFiltres());
        filtreCategorieCombo.valueProperty().addListener((o, ov, nv) -> appliquerFiltres());
    }

    private void configurerSelection() {
        tachesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            afficherDetails(sel);
            chargerSousTaches(sel);
            boolean ok = sel != null;
            btnSupprimer.setDisable(!ok);
            btnTerminer.setDisable(!ok || sel.getEtat() == EtatTache.TERMINEE);
            btnDecomposerIA.setDisable(!ok);
        });
    }

    private void configurerSousTachesView() {
        sousTachesList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SousTache st, boolean empty) {
                super.updateItem(st, empty);
                if (empty || st == null) { setText(null); setGraphic(null); return; }
                boolean done = st.getEtat() == EtatTache.TERMINEE;
                CheckBox cb = new CheckBox(st.getTitre());
                cb.setSelected(done);
                cb.setDisable(true);
                cb.setStyle(done ? "-fx-text-fill:#9CA3AF;" : "-fx-text-fill:#111827;-fx-font-weight:600;");
                setGraphic(cb);
                setText(null);
                setStyle("-fx-padding:6 10;");
            }
        });
    }

    private void reload() {
        if (utilisateur == null) return;
        List<Tache> liste = ts.getTaches(utilisateur.getId());
        allTaches.setAll(liste);
        appliquerFiltres();
        nbTachesLabel.setText(liste.size() + " tâche(s)");
    }

    private void appliquerFiltres() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String etat = filtreEtatCombo.getValue();
        String prio = filtrePrioriteCombo.getValue();
        String cat = filtreCategorieCombo.getValue();

        List<Tache> res = allTaches.stream()
                .filter(t -> q.isEmpty() || contains(t.getTitre(), q) || contains(t.getDescription(), q))
                .filter(t -> "TOUS".equals(etat) || t.getEtat().name().equals(etat))
                .filter(t -> "TOUS".equals(prio) || t.getPriorite().name().equals(prio))
                .filter(t -> {
                    if ("TOUS".equals(cat)) return true;
                    int id = t.getIdCategorie();
                    String nom = (id >= 1 && id <= CATS.length) ? CATS[id - 1] : "—";
                    return nom.equals(cat);
                })
                .collect(Collectors.toList());

        tachesTable.setItems(FXCollections.observableArrayList(res));

        if (res.isEmpty()) {
            setDetailsEmpty();
            sousTachesList.setItems(FXCollections.observableArrayList());
        }
    }

    private static boolean contains(String s, String q) {
        return s != null && s.toLowerCase().contains(q);
    }

    private void afficherDetails(Tache t) {
        if (t == null) { setDetailsEmpty(); return; }
        hintSelectionLabel.setVisible(false);
        hintSelectionLabel.setManaged(false);

        detailTitreLabel.setText(nvl(t.getTitre()));
        detailPrioriteLabel.setText(t.getPriorite() != null ? t.getPriorite().name() : "—");
        detailEtatLabel.setText(t.getEtat() != null ? t.getEtat().name().replace("_", " ") : "—");

        int id = t.getIdCategorie();
        detailCategorieLabel.setText((id >= 1 && id <= CATS.length) ? CATS[id - 1] : "—");
        detailEcheanceLabel.setText(t.getEcheance() != null ? t.getEcheance().format(DF) : "—");
    }

    private void chargerSousTaches(Tache t) {
        if (t == null) {
            sousTachesList.setItems(FXCollections.observableArrayList());
            nbSousTachesLabel.setText("");
            return;
        }
        List<SousTache> liste = sts.getSousTaches(t.getId());
        sousTachesList.setItems(FXCollections.observableArrayList(liste));
        nbSousTachesLabel.setText(liste.size() + " sous-tâche(s)");
    }

    private void setDetailsEmpty() {
        detailTitreLabel.setText("—");
        detailPrioriteLabel.setText("—");
        detailEtatLabel.setText("—");
        detailCategorieLabel.setText("—");
        detailEcheanceLabel.setText("—");
        nbSousTachesLabel.setText("");
        btnTerminer.setDisable(true);
        btnSupprimer.setDisable(true);
        btnDecomposerIA.setDisable(true);
        hintSelectionLabel.setVisible(true);
        hintSelectionLabel.setManaged(true);
    }

    @FXML
    public void ouvrirFormulaire() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/nouvelle_tache.fxml"));
            Parent root = loader.load();

            NouvelleTacheController ctrl = loader.getController();
            ctrl.setUtilisateur(utilisateur);
            ctrl.setOnSuccess(() -> {
                msg("✓ Tâche créée avec succès.", true);
                reload();
            });

            Stage stage = new Stage();
            stage.setTitle("Nouvelle tâche");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            msg("Impossible d'ouvrir le formulaire.", false);
        }
    }

    @FXML
    public void terminerSelection() {
        Tache t = tachesTable.getSelectionModel().getSelectedItem();
        if (t == null) { msg("Sélectionnez une tâche.", false); return; }
        ts.terminerTache(t.getId());
        msg("✓ Tâche terminée.", true);
        reload();
    }

    @FXML
    public void supprimerSelection() {
        Tache t = tachesTable.getSelectionModel().getSelectedItem();
        if (t == null) { msg("Sélectionnez une tâche.", false); return; }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer '" + t.getTitre() + "' ?",
                ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                ts.supprimerTache(t.getId());
                msg("Tâche supprimée.", true);
                reload();
            }
        });
    }

    @FXML
    public void ajouterSousTache() {
        Tache t = tachesTable.getSelectionModel().getSelectedItem();
        if (t == null) { msg("Sélectionnez une tâche.", false); return; }

        String titre = nouvelleSousTacheField.getText() == null ? "" : nouvelleSousTacheField.getText().trim();
        if (titre.isEmpty()) { msg("Saisissez le titre de la sous-tâche.", false); return; }

        boolean ok = sts.ajouterSousTache(titre, t.getId());
        if (ok) {
            nouvelleSousTacheField.clear();
            chargerSousTaches(t);
            msg("✓ Sous-tâche ajoutée.", true);
        } else {
            msg("Erreur lors de l'ajout.", false);
        }
    }

    /**
     * NOUVEAU : Décomposer une tâche en sous-tâches via IA Gemini
     */
    @FXML
    public void decomposerAvecIA() {
        Tache t = tachesTable.getSelectionModel().getSelectedItem();
        if (t == null) { msg("Sélectionnez une tâche.", false); return; }

        // Vérifie si la tâche a déjà des sous-tâches
        List<SousTache> existantes = sts.getSousTaches(t.getId());
        if (!existantes.isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Cette tâche a déjà " + existantes.size() + " sous-tâche(s).\n" +
                            "L'IA va en ajouter de nouvelles. Continuer ?",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText("Sous-tâches existantes");
            var rep = confirm.showAndWait();
            if (rep.isEmpty() || rep.get() != ButtonType.OK) return;
        }

        // Désactive le bouton et affiche chargement
        btnDecomposerIA.setDisable(true);
        btnDecomposerIA.setText("⏳ L'IA réfléchit...");
        msg("Génération en cours par l'IA...", true);

        // Lance dans un thread pour ne pas bloquer l'UI
        new Thread(() -> {
            String prompt = "Décompose cette tâche en 5 à 7 sous-tâches courtes, " +
                    "concrètes et actionnables. Format : une sous-tâche par ligne, " +
                    "numérotées (1. 2. 3.). Pas de titre ni d'introduction. " +
                    "Tâche : " + t.getTitre();

            String reponse = ias.decomposerTache(prompt);

            Platform.runLater(() -> {
                btnDecomposerIA.setDisable(false);
                btnDecomposerIA.setText("✨ Décomposer avec l'IA");

                if (reponse == null || reponse.isBlank()) {
                    msg("L'IA n'a pas pu répondre.", false);
                    return;
                }

                // Parser la réponse en lignes
                List<String> sousTaches = parserReponseIA(reponse);

                if (sousTaches.isEmpty()) {
                    msg("L'IA n'a pas généré de sous-tâches valides.", false);
                    return;
                }

                // Enregistrer chaque sous-tâche
                int compteur = 0;
                for (String titreST : sousTaches) {
                    if (sts.ajouterSousTache(titreST, t.getId())) {
                        compteur++;
                    }
                }

                chargerSousTaches(t);
                msg("✨ " + compteur + " sous-tâche(s) générée(s) par l'IA !", true);
            });
        }).start();
    }

    /**
     * Parse la réponse de l'IA en liste de sous-tâches propres
     */
    private List<String> parserReponseIA(String reponse) {
        List<String> resultat = new ArrayList<>();
        String[] lignes = reponse.split("\\n");

        for (String ligne : lignes) {
            String l = ligne.trim();
            if (l.isEmpty()) continue;

            // Supprime les puces : "1.", "2)", "•", "-", "*", etc.
            l = l.replaceAll("^[\\d]+[\\.\\)]\\s*", "");
            l = l.replaceAll("^[•\\-\\*]\\s*", "");
            l = l.trim();

            // Ignore les lignes trop courtes ou trop longues
            if (l.length() < 3 || l.length() > 200) continue;

            // Ignore les lignes qui ressemblent à des titres/intros
            String lower = l.toLowerCase();
            if (lower.startsWith("voici") || lower.startsWith("plan") ||
                    lower.startsWith("décomposition") || lower.contains("sous-tâche")) continue;

            resultat.add(l);

            // Limite à 10 max
            if (resultat.size() >= 10) break;
        }

        return resultat;
    }

    @FXML
    public void resetFiltres() {
        searchField.clear();
        filtreEtatCombo.setValue("TOUS");
        filtrePrioriteCombo.setValue("TOUS");
        filtreCategorieCombo.setValue("TOUS");
    }

    private Label badge(String txt, String bg, String fg) {
        Label l = new Label(txt);
        l.setStyle("-fx-background-color:" + bg + ";" +
                "-fx-text-fill:" + fg + ";" +
                "-fx-font-size:10px;-fx-font-weight:800;" +
                "-fx-padding:4 10;-fx-background-radius:20;");
        return l;
    }

    private void msg(String txt, boolean ok) {
        messageLabel.setStyle("-fx-text-fill:" + (ok ? "#059669" : "#DC2626") +
                ";-fx-font-size:12px;-fx-font-weight:800;");
        messageLabel.setText(txt == null ? "" : txt);
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String pBg(String p) {
        return switch (p) {
            case "HAUTE"   -> "#FEE2E2";
            case "MOYENNE" -> "#FEF3C7";
            default        -> "#D1FAE5";
        };
    }

    private String pFg(String p) {
        return switch (p) {
            case "HAUTE"   -> "#991B1B";
            case "MOYENNE" -> "#92400E";
            default        -> "#065F46";
        };
    }

    private String eBg(EtatTache e) {
        return switch (e) {
            case TERMINEE -> "#D1FAE5";
            case EN_COURS -> "#E0E7FF";
            case ANNULEE  -> "#FEE2E2";
            default       -> "#F3F4F6";
        };
    }

    private String eFg(EtatTache e) {
        return switch (e) {
            case TERMINEE -> "#065F46";
            case EN_COURS -> "#3730A3";
            case ANNULEE  -> "#991B1B";
            default       -> "#6B7280";
        };
    }
}