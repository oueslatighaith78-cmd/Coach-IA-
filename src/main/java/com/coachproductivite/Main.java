package com.coachproductivite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlLocation = getClass().getResource("/fxml/login.fxml");

        if (fxmlLocation == null) {
            System.err.println("ERREUR : Le fichier /fxml/login.fxml est introuvable !");
            System.err.println("Vérifiez qu'il est bien dans src/main/resources/fxml/login.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        // Taille augmentée pour le nouveau design 2 colonnes
        Scene scene = new Scene(loader.load(), 1000, 650);

        stage.setTitle("Coach Productivité");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}