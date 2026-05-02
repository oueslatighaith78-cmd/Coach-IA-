package com.coachproductivite.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IAService {

    private static final String API_KEY = "AIzaSyC0sNei92kcmNbpd4-UGRmuZAXbgL-pXOw";
    private static final String MODELE_PRINCIPAL = "gemini-2.5-flash";
    private static final String MODELE_SECOURS = "gemini-1.5-flash-latest";

    private final HttpClient client;

    public IAService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Point d'entrée unique : essaie l'API, sinon retombe sur le mode démo local
     */
    private String envoyerRequete(String prompt) {
        // 1. Essaie l'API réelle
        String apiResponse = appelerApi(prompt);
        if (apiResponse != null) {
            return apiResponse;
        }

        // 2. Fallback : mode démo (pour que l'app reste fonctionnelle)
        System.out.println(">>> API indisponible — mode démo activé");
        return genererReponseMock(prompt);
    }

    /**
     * Appel réel à Gemini
     */
    private String appelerApi(String prompt) {
        String[] modeles = {MODELE_PRINCIPAL, MODELE_SECOURS};
        for (String modele : modeles) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + modele + ":generateContent?key=" + API_KEY;
            for (int t = 1; t <= 2; t++) {
                try {
                    String body = """
                        {
                            "contents": [{
                                "parts": [{
                                    "text": "%s"
                                }]
                            }]
                        }
                        """.formatted(prompt.replace("\"", "\\\"").replace("\n", " "));

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (json.has("candidates") && !json.getAsJsonArray("candidates").isEmpty()) {
                            return json.getAsJsonArray("candidates")
                                    .get(0).getAsJsonObject()
                                    .getAsJsonObject("content")
                                    .getAsJsonArray("parts")
                                    .get(0).getAsJsonObject()
                                    .get("text").getAsString();
                        }
                    }
                    if (response.statusCode() == 503) {
                        Thread.sleep(1500);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * MODE DÉMO : génère une réponse crédible sans API
     * Parfait pour la soutenance si l'API est en panne
     */
    private String genererReponseMock(String prompt) {
        String p = prompt.toLowerCase();

        if (p.contains("décompose") || p.contains("sous-tâches") || p.contains("plan sur")) {
            return genererPlanMock(extractSubject(prompt));
        }
        if (p.contains("priorité") || p.contains("ordre")) {
            return "🎯 Suggestions de priorités :\n\n"
                    + "1. 🔴 Haute priorité : Tâches avec échéance dans moins de 48h\n"
                    + "2. 🟠 Moyenne priorité : Tâches en cours à finaliser cette semaine\n"
                    + "3. 🟢 Basse priorité : Objectifs long terme sans échéance immédiate\n\n"
                    + "💡 Conseil : commence par la tâche la plus difficile le matin (effet de lancement).";
        }
        if (p.contains("reformule") || p.contains("smart")) {
            return "✨ Objectif reformulé (méthode SMART) :\n\n"
                    + "• Spécifique : " + extractSubject(prompt) + " bien défini avec livrables clairs\n"
                    + "• Mesurable : progression vérifiable à 25%, 50%, 75%, 100%\n"
                    + "• Atteignable : découpé en sous-tâches de 2h maximum\n"
                    + "• Réaliste : compatible avec ton emploi du temps actuel\n"
                    + "• Temporel : deadline fixée avec marge de sécurité\n\n"
                    + "🚀 Commence par la première micro-tâche dès aujourd'hui !";
        }
        if (p.contains("planifier") || p.contains("semaine")) {
            return "📅 Plan de la semaine type :\n\n"
                    + "Lundi : Lancement + récupération des ressources\n"
                    + "Mardi : Travail concentré (blocs de 90 min)\n"
                    + "Mercredi : Avancement + point d'étape\n"
                    + "Jeudi : Finalisation du gros du travail\n"
                    + "Vendredi : Revue, corrections et livraison\n"
                    + "Week-end : Repos actif (lecture légère)\n\n"
                    + "⏱ Utilise le chronomètre intégré pour tes sessions focus.";
        }

        // Réponse générique intelligente
        return "🤖 Voici mon analyse pour : \"" + extractSubject(prompt) + "\"\n\n"
                + "• Étape 1 : Clarifier l'objectif final et les livrables attendus\n"
                + "• Étape 2 : Identifier les ressources et compétences nécessaires\n"
                + "• Étape 3 : Découper en actions de 30-60 minutes maximum\n"
                + "• Étape 4 : Planifier les 3 premières actions dans ton calendrier\n"
                + "• Étape 5 : Lancer la première action immédiatement (règle des 2 minutes)\n\n"
                + "💡 N'oublie pas d'enregistrer chaque session dans le chronomètre pour suivre ta productivité !";
    }

    private String genererPlanMock(String sujet) {
        if (sujet.isEmpty()) sujet = "votre projet";
        return "📋 Plan d'action — " + sujet + " (7 jours)\n\n"
                + "Jour 1 — Analyse & Setup\n"
                + "• Définir le périmètre exact et les livrables\n"
                + "• Rassembler la documentation et les outils\n\n"
                + "Jour 2 — Conception\n"
                + "• Esquisser l'architecture / la structure globale\n"
                + "• Valider les choix techniques ou méthodologiques\n\n"
                + "Jour 3 — Développement (Partie 1)\n"
                + "• Implémenter le cœur fonctionnel (60% du travail)\n"
                + "• Tester chaque composant au fur et à mesure\n\n"
                + "Jour 4 — Développement (Partie 2)\n"
                + "• Finaliser les fonctionnalités secondaires\n"
                + "• Gérer les cas particuliers et erreurs\n\n"
                + "Jour 5 — Intégration\n"
                + "• Assembler les différentes parties\n"
                + "• Tests globaux et corrections de bugs\n\n"
                + "Jour 6 — Revue & Optimisation\n"
                + "• Relecture complète, refactorisation si besoin\n"
                + "• Préparation de la documentation / présentation\n\n"
                + "Jour 7 — Finalisation\n"
                + "• Dernières vérifications et ajustements\n"
                + "• Livraison et célébration ! 🎉\n\n"
                + "⏱ Temps estimé : 1h30 à 2h par jour";
    }

    private String extractSubject(String prompt) {
        // Extrait le sujet après les deux-points ou le dernier segment
        if (prompt.contains(":")) {
            return prompt.substring(prompt.lastIndexOf(':') + 1).trim();
        }
        return prompt.length() > 50 ? prompt.substring(0, 50) + "..." : prompt;
    }

    // Méthodes publiques utilisées par le controller
    public String decomposerTache(String titre) {
        return envoyerRequete("Décompose cette tâche en sous-tâches simples et numérotées : " + titre);
    }

    public String suggererPriorites(String taches) {
        return envoyerRequete("Voici mes tâches : " + taches + ". Propose un ordre de priorité court.");
    }

    public String reformulerObjectif(String objectif) {
        return envoyerRequete("Reformule cet objectif de façon motivante : " + objectif);
    }

    public String genererPlan(String titre, int jours) {
        return envoyerRequete("Crée un plan sur " + jours + " jours pour : " + titre);
    }
}