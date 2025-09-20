package com.algoarena.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Set up SceneManager
            SceneManager.getInstance().setPrimaryStage(primaryStage);

            // Load initial scene
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/fxml/homescreen.fxml")));
            Scene scene = new Scene(root);

            primaryStage.setTitle("AlgoArena");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
