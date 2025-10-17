package com.algoarena.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import com.algoarena.client.audio.Bgm;

public class HomePageController {

    @FXML
    private Label gameTitle;

    @FXML
    private Button startGameButton;

    @FXML
    public void handleStartGame(ActionEvent event) {
        // Create fade and scale transition for smooth exit
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), gameTitle);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition buttonFadeOut = new FadeTransition(Duration.seconds(1), startGameButton);
        buttonFadeOut.setFromValue(1.0);
        buttonFadeOut.setToValue(0.0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.seconds(1), startGameButton);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        ParallelTransition exitAnimation = new ParallelTransition(fadeOut, buttonFadeOut, scaleOut);

        // After animation completes, switch to game mode selection
        exitAnimation.setOnFinished(e -> {
            SceneManager.getInstance().switchToScene("gamemode.fxml");
        });
        exitAnimation.play();
    }

    @FXML
    private void initialize() {
        System.out.println("AlgoArena Home Page loaded successfully!");
        setupButtonStyles();
        setupButtonAnimations();
        Bgm.ensureStarted();
    }

    private void setupButtonStyles() {
        // Main start button - Pink/Purple gaming theme
        String startButtonStyle = """
            -fx-background-color: linear-gradient(to bottom, #E91E63 0%, #C2185B 50%, #AD1457 100%);
            -fx-background-radius: 25px;
            -fx-border-color: #880E4F;
            -fx-border-width: 4px;
            -fx-border-radius: 25px;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0.4, 0, 5);
            -fx-cursor: hand;
            """;
        startGameButton.setStyle(startButtonStyle);
    }

    private void setupButtonAnimations() {
        // Hover effect for start button
        startGameButton.setOnMouseEntered(e -> {
            String hoverStyle = """
                -fx-background-color: linear-gradient(to bottom, #F06292 0%, #E91E63 50%, #C2185B 100%);
                -fx-background-radius: 25px;
                -fx-border-color: #880E4F;
                -fx-border-width: 4px;
                -fx-border-radius: 25px;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0.5, 0, 7);
                -fx-cursor: hand;
                """;
            startGameButton.setStyle(hoverStyle);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), startGameButton);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
        });

        startGameButton.setOnMouseExited(e -> {
            setupButtonStyles(); // Reset to original style

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), startGameButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }
}
