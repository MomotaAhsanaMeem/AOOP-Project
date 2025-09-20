package com.algoarena.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class HomePageController {

    @FXML
    private Label gameTitle;

    @FXML
    private Button startGameButton;

    @FXML
    public void handleStartGame(ActionEvent event) {
        // Create fade transition
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), gameTitle);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition buttonFadeOut = new FadeTransition(Duration.seconds(1), startGameButton);
        buttonFadeOut.setFromValue(1.0);
        buttonFadeOut.setToValue(0.0);

        // After fade completes, switch to game mode selection
        fadeOut.setOnFinished(e -> {
            SceneManager.getInstance().switchToScene("gamemode.fxml");
        });

        fadeOut.play();
        buttonFadeOut.play();
    }

    @FXML
    private void initialize() {
        System.out.println("AlgoArena Home Page loaded successfully!");
    }
}
