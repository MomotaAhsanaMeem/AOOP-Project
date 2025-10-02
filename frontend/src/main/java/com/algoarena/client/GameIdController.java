package com.algoarena.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class GameIdController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    private Button startButton;

    @FXML
    public void handleStart(ActionEvent event) {
        String playerName = nameTextField.getText().trim();

        // Validate input
        if (playerName.isEmpty()) {
            showAlert("Error", "Please enter your name to continue!");
            return;
        }

        // Store the player name for later use
        System.out.println("Player name: " + playerName);

        // Create fade out effect before transitioning to story
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), titleLabel.getParent());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // After fade completes, switch to story scene
        fadeOut.setOnFinished(e -> {
            SceneManager.getInstance().switchToScene("story.fxml");
        });

        fadeOut.play();
    }

    @FXML
    public void handleBack(ActionEvent event) {
        SceneManager.getInstance().switchToScene("gamemode.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String getPlayerName() {
        return nameTextField.getText().trim();
    }

    @FXML
    private void initialize() {
        System.out.println("Game ID page loaded successfully!");
        nameTextField.setPromptText("Enter your name here...");
        nameTextField.setOnAction(this::handleStart);
    }
}
