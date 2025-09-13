package com.algoarena.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HomePageController {

    @FXML
    private Label gameTitle;

    @FXML
    private Button startGameButton;

    @FXML
    public void handleStartGame(ActionEvent event) {
        // Hide the game title and start button when clicked
        gameTitle.setVisible(false);
        startGameButton.setVisible(false);

        // You can add further actions here such as showing story text or starting the game
    }

    @FXML
    private void initialize() {
        System.out.println("AlgoArena Home Page loaded successfully!");
    }
}
