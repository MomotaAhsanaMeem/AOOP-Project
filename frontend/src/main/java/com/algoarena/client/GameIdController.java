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

import com.algoarena.client.net.AppContext;
import com.algoarena.client.net.MessageListener;

public class GameIdController {

    @FXML private Label titleLabel;        // must match fx:id in FXML
    @FXML private TextField nameTextField; // must match fx:id in FXML
    @FXML private Button startButton;      // must match fx:id in FXML

    @FXML
    public void handleStart(ActionEvent event) {
        // 1) Validate name
        String playerName = (nameTextField.getText() == null) ? "" : nameTextField.getText().trim();
        if (playerName.isEmpty()) {
            showAlert("Error", "Please enter your name to continue!");
            return;
        }

        // 2) Save name globally (used by later scenes/controllers)
        AppContext.setPlayerName(playerName);

        // 3) Start socket connection in the background (non-blocking)
        //    Register listener first (no UI updates here to avoid touching disposed nodes)
        AppContext.net().setListener(new MessageListener() {
            @Override public void onOpen(String playerId, String sessionId) {
                // Successfully connected — no UI updates needed here
                // (we may already be on another scene)
            }
            @Override public void onError(String code, String message) {
                // Optional: log or route to some global toaster; avoid touching current scene’s nodes
                System.err.println("WS error: " + code + " - " + message);
            }
        });
        AppContext.net().connect("ws://localhost:8080/ws", playerName, 1);

        // 4) Immediately run your exit animation and go to the next scene (original behavior)
        playExitAnimationAndGoToStory();
    }

    @FXML
    public void handleBack(ActionEvent event) {
        SceneManager.getInstance().switchToScene("gamemode.fxml");
    }

    @FXML
    private void initialize() {
        if (nameTextField != null) {
            nameTextField.setPromptText("Enter your name here...");
            // Allow Enter to trigger Start
            nameTextField.setOnAction(this::handleStart);
        }
        setupStyles();
        setupAnimations();

        if (titleLabel != null) {
            titleLabel.setText("Enter your name and press Start");
        }
    }

    // ---------- UI helpers ----------

    private void playExitAnimationAndGoToStory() {
        // Defensive: parent can be null if label isn’t in scene graph yet
        var container = (titleLabel != null) ? titleLabel.getParent() : null;

        FadeTransition fadeOut = null;
        if (container != null) {
            fadeOut = new FadeTransition(Duration.seconds(1), container);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
        }

        ScaleTransition scaleOut = new ScaleTransition(Duration.seconds(1), startButton);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        ParallelTransition exit = (fadeOut != null)
                ? new ParallelTransition(fadeOut, scaleOut)
                : new ParallelTransition(scaleOut);

        exit.setOnFinished(e -> SceneManager.getInstance().switchToScene("story.fxml"));
        exit.play();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String getPlayerName() {
        return nameTextField != null ? nameTextField.getText().trim() : "";
    }

    private void setupStyles() {
        if (nameTextField != null) {
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
        }
        if (startButton != null) {
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
    }

    private void setupAnimations() {
        if (nameTextField != null) {
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
            nameTextField.setOnMouseExited(e -> setupStyles());
        }

        if (startButton != null) {
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
}
