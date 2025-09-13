package com.algoarena.client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class HomePageController {
    @FXML
    private Label gameTitle;

    @FXML
    private Button startGameButton;

    @FXML
    private void handleStartGame(ActionEvent event) {
        try {
            // Since you don't have gamescreen.fxml yet, just show a message
            System.out.println("Starting AlgoArena Game!");

            // Temporary alert instead of scene switching
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("AlgoArena");
            alert.setHeaderText(null);
            alert.setContentText("Game will start soon!");
            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("Error loading game screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        System.out.println("AlgoArena Home Page loaded successfully!");
    }
}
