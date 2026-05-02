package com.coachproductivite.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Ajout de serverTimezone pour éviter des erreurs avec MySQL 8
    private static final String URL      = "jdbc:mysql://localhost:3306/coach_productivite?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static DatabaseConnection instance;
    private Connection connexion;

    private DatabaseConnection() {
        try {
            // Chargement explicite du driver (optionnel mais plus sûr)
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion MySQL réussie !");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnexion() {
        return connexion;
    }

    public void fermer() {
        try {
            if (connexion != null && !connexion.isClosed()) {
                connexion.close();
                System.out.println("Connexion fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture : " + e.getMessage());
        }
    }
}