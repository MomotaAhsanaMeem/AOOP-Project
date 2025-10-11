package com.algoarena.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
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

        // Create beautiful exit animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), titleLabel.getParent());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.seconds(1), startButton);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        ParallelTransition exitAnimation = new ParallelTransition(fadeOut, scaleOut);

        // After fade completes, switch to story scene
        exitAnimation.setOnFinished(e -> {
            SceneManager.getInstance().switchToScene("story.fxml");
        });
        exitAnimation.play();
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
        nameTextField.setOnAction(this::handleStart); // Allow Enter key to start

        setupStyles();
        setupAnimations();
    }

    private void setupStyles() {
        // Style the text field
        String textFieldStyle = """
            -fx-background-color: rgba(255,255,255,0.9);
            -fx-background-radius: 15px;
            -fx-border-color: #4CAF50;
            -fx-border-width: 2px;
            -fx-border-radius: 15px;
            -fx-padding: 10px 15px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.2, 0, 2);
            """;
        nameTextField.setStyle(textFieldStyle);

        // Style the start button
        String startButtonStyle = """
            -fx-background-color: linear-gradient(to bottom, #4CAF50 0%, #45a049 50%, #3d8b40 100%);
            -fx-background-radius: 25px;
            -fx-border-color: #2e7d32;
            -fx-border-width: 3px;
            -fx-border-radius: 25px;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0.3, 0, 4);
            -fx-cursor: hand;
            """;
        startButton.setStyle(startButtonStyle);
    }

    private void setupAnimations() {
        // Text field focus effects
        nameTextField.setOnMouseEntered(e -> {
            String hoverStyle = """
                -fx-background-color: rgba(255,255,255,0.95);
                -fx-background-radius: 15px;
                -fx-border-color: #66BB6A;
                -fx-border-width: 3px;
                -fx-border-radius: 15px;
                -fx-padding: 10px 15px;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.3, 0, 3);
                """;
            nameTextField.setStyle(hoverStyle);
        });

        nameTextField.setOnMouseExited(e -> {
            if (!nameTextField.isFocused()) {
                setupStyles();
            }
        });

        // Start button hover effects
        startButton.setOnMouseEntered(e -> {
            String hoverStyle = """
                -fx-background-color: linear-gradient(to bottom, #66BB6A 0%, #4CAF50 50%, #43A047 100%);
                -fx-background-radius: 25px;
                -fx-border-color: #2e7d32;
                -fx-border-width: 3px;
                -fx-border-radius: 25px;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 15, 0.4, 0, 6);
                -fx-cursor: hand;
                """;
            startButton.setStyle(hoverStyle);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), startButton);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
        });

        startButton.setOnMouseExited(e -> {
            setupStyles();

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), startButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });

        // Button press effect
        startButton.setOnMousePressed(e -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), startButton);
            scalePress.setToX(0.95);
            scalePress.setToY(0.95);
            scalePress.play();
        });

        startButton.setOnMouseReleased(e -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), startButton);
            scaleRelease.setToX(1.05);
            scaleRelease.setToY(1.05);
            scaleRelease.play();
        });
    }
}
