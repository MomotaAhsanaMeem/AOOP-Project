package com.algoarena.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

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

        // Store the player name (you can use this later in other scenes)
        System.out.println("Player name: " + playerName);

        // Switch to the actual gameplay scene
        SceneManager.getInstance().switchToScene("story.fxml");
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

    // Method to get the entered name (can be used by other controllers)
    public String getPlayerName() {
        return nameTextField.getText().trim();
    }

    @FXML
    private void initialize() {
        System.out.println("Game ID page loaded successfully!");

        // Set prompt text for the TextField
        nameTextField.setPromptText("Enter your name here...");

        // Add listener for Enter key on TextField
        nameTextField.setOnAction(this::handleStart);
    }
}
