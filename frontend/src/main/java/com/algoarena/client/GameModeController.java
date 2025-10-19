package com.algoarena.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class GameModeController {

    @FXML
    private Button singlePlayerButton;

    // @FXML
    // private Button buddyOpButton;

    @FXML
    private Button backButton;

    @FXML
    public void handleSinglePlayer(ActionEvent event) {
        SceneManager.getInstance().switchToScene("gameid.fxml");
    }

    // @FXML
    // public void handleBuddyOp(ActionEvent event) {
    //     // SceneManager.getInstance().switchToScene("gameid.fxml");
    // }

    @FXML
    public void handleBack(ActionEvent event) {
        SceneManager.getInstance().switchToScene("homescreen.fxml");
    }

    @FXML
    private void initialize() {
        System.out.println("Game Mode Selection loaded!");
        setupButtonStyles();
        setupButtonAnimations();
    }

    private void setupButtonStyles() {
        // Single Player button - Blue theme
        String singlePlayerStyle = """
            -fx-background-color: linear-gradient(to bottom, #2196F3 0%, #1976D2 50%, #1565C0 100%);
            -fx-background-radius: 20px;
            -fx-border-color: #0D47A1;
            -fx-border-width: 3px;
            -fx-border-radius: 20px;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0.3, 0, 4);
            -fx-cursor: hand;
            """;
        singlePlayerButton.setStyle(singlePlayerStyle);

        // Buddy OP button - Orange theme
        // String buddyOpStyle = """
        //     -fx-background-color: linear-gradient(to bottom, #FF9800 0%, #F57C00 50%, #E65100 100%);
        //     -fx-background-radius: 20px;
        //     -fx-border-color: #BF360C;
        //     -fx-border-width: 3px;
        //     -fx-border-radius: 20px;
        //     -fx-text-fill: white;
        //     -fx-font-weight: bold;
        //     -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0.3, 0, 4);
        //     -fx-cursor: hand;
        //     """;
        // buddyOpButton.setStyle(buddyOpStyle);

        // Back button - Gray theme
        String backStyle = """
            -fx-background-color: linear-gradient(to bottom, #757575 0%, #616161 50%, #424242 100%);
            -fx-background-radius: 15px;
            -fx-border-color: #212121;
            -fx-border-width: 2px;
            -fx-border-radius: 15px;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0.2, 0, 3);
            -fx-cursor: hand;
            """;
        backButton.setStyle(backStyle);
    }

    private void setupButtonAnimations() {
        // Single Player hover effects
        singlePlayerButton.setOnMouseEntered(e -> {
            String hoverStyle = """
                -fx-background-color: linear-gradient(to bottom, #42A5F5 0%, #2196F3 50%, #1976D2 100%);
                -fx-background-radius: 20px;
                -fx-border-color: #0D47A1;
                -fx-border-width: 3px;
                -fx-border-radius: 20px;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 18, 0.4, 0, 6);
                -fx-cursor: hand;
                """;
            singlePlayerButton.setStyle(hoverStyle);
            scaleButton(singlePlayerButton, 1.05);
        });

        singlePlayerButton.setOnMouseExited(e -> {
            setupButtonStyles();
            scaleButton(singlePlayerButton, 1.0);
        });

        // Buddy OP hover effects
        // buddyOpButton.setOnMouseEntered(e -> {
        //     String hoverStyle = """
        //         -fx-background-color: linear-gradient(to bottom, #FFB74D 0%, #FF9800 50%, #F57C00 100%);
        //         -fx-background-radius: 20px;
        //         -fx-border-color: #BF360C;
        //         -fx-border-width: 3px;
        //         -fx-border-radius: 20px;
        //         -fx-text-fill: white;
        //         -fx-font-weight: bold;
        //         -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 18, 0.4, 0, 6);
        //         -fx-cursor: hand;
        //         """;
        //     buddyOpButton.setStyle(hoverStyle);
        //     scaleButton(buddyOpButton, 1.05);
        // });

        // buddyOpButton.setOnMouseExited(e -> {
        //     setupButtonStyles();
        //     scaleButton(buddyOpButton, 1.0);
        // });

        // Back button hover effects
        backButton.setOnMouseEntered(e -> {
            String hoverStyle = """
                -fx-background-color: linear-gradient(to bottom, #9E9E9E 0%, #757575 50%, #616161 100%);
                -fx-background-radius: 15px;
                -fx-border-color: #212121;
                -fx-border-width: 2px;
                -fx-border-radius: 15px;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0.3, 0, 4);
                -fx-cursor: hand;
                """;
            backButton.setStyle(hoverStyle);
            scaleButton(backButton, 1.03);
        });

        backButton.setOnMouseExited(e -> {
            setupButtonStyles();
            scaleButton(backButton, 1.0);
        });
    }

    private void scaleButton(Button button, double scale) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);
        scaleTransition.play();
    }
}
