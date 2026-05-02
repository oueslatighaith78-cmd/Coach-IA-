package com.coachproductivite.model;

public class Utilisateur {

    private int id;
    private String nom;
    private String email;
    private String motDePasse;
    private String objectifs;           // Pour la page Profil
    private String rythmeTravail;       // Pour les préférences

    // ==================== Constructeurs ====================

    public Utilisateur() {}

    public Utilisateur(String nom, String email, String motDePasse) {
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    public Utilisateur(int id, String nom, String email, String motDePasse) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    // Constructeur complet (utile pour le futur)
    public Utilisateur(int id, String nom, String email, String motDePasse,
                       String objectifs, String rythmeTravail) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.objectifs = objectifs;
        this.rythmeTravail = rythmeTravail;
    }

    // ==================== Getters & Setters ====================

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getMotDePasse() { return motDePasse; }
    public String getObjectifs() { return objectifs; }
    public String getRythmeTravail() { return rythmeTravail; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; }
    public void setRythmeTravail(String rythmeTravail) { this.rythmeTravail = rythmeTravail; }

    // ==================== Méthodes utilitaires ====================

    /**
     * Retourne les initiales de l'utilisateur pour l'avatar
     */
    public String getInitiales() {
        if (nom == null || nom.trim().isEmpty()) {
            return "??";
        }
        String n = nom.trim();
        StringBuilder initiales = new StringBuilder();

        String[] parts = n.split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty() && initiales.length() < 2) {
                initiales.append(part.charAt(0));
            }
        }

        String result = initiales.toString().toUpperCase();
        return result.length() >= 2 ? result : result + "?";
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", objectifs='" + objectifs + '\'' +
                ", rythmeTravail='" + rythmeTravail + '\'' +
                '}';
    }
}