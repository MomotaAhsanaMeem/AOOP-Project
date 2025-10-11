package com.algoarena.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.animation.*;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class StoryController {

    @FXML
    private VBox storyContainer;
    @FXML
    private Text storyText;
    @FXML
    private Button continueButton;
    @FXML
    private ImageView characterImage;

    // Create loading label programmatically (no need for FXML)
    private Label loadingLabel;

    // Store animation frames
    private List<Image> runningFrames;
    private Timeline spriteAnimation;
    private Timeline loadingAnimation;

    @FXML
    private void initialize() {
        storyText.setText("\n" +
                "In a distant, advanced future, a gifted young coder discovered an ancient device buried in the ruins. While experimenting with its mysterious mechanisms, he triggered a strange anomaly and was hurled backward through time into a primitive past. Struggling to adapt to this harsh world, he encounters a brilliant professor who reveals a powerful machine that could send him back to his future timeline.\n\n" +
                "The professor warns the path won't be simple. To restore the machine, he must journey through three perilous domains—each holding a vital piece to bring the device back to life. Though the trials are daunting in this primitive world, his superior knowledge of algorithms and advanced reasoning from the future give him the edge to face every challenge that awaits.");
        storyText.setFont(Font.font("Lucida Console", 20));

        // Initially hide the character image
        characterImage.setVisible(false);

        // Create loading label programmatically
        createLoadingLabel();

        // Load running animation frames
        loadRunningFrames();

        // Setup beautiful button styling
        setupContinueButtonStyles();
        setupContinueButtonAnimations();
    }

    private void setupContinueButtonStyles() {
        // Beautiful continue button with proper sizing and alignment
        String continueButtonStyle = """
        -fx-background-color: linear-gradient(to bottom, #9C27B0 0%, #7B1FA2 50%, #6A1B9A 100%);
        -fx-background-radius: 20px;
        -fx-border-color: #4A148C;
        -fx-border-width: 3px;
        -fx-border-radius: 20px;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0.4, 0, 4);
        -fx-cursor: hand;
        -fx-font-size: 14px;
        -fx-alignment: center;
        -fx-text-alignment: center;
        -fx-content-display: center;
        """;
        continueButton.setStyle(continueButtonStyle);
        continueButton.setText("🚀 CONTINUE");
        continueButton.setPrefHeight(55);
        continueButton.setPrefWidth(180);
        continueButton.setMaxWidth(180);
        continueButton.setMinWidth(180);

        // Ensure text wrapping is disabled and alignment is centered
        continueButton.setWrapText(false);
        continueButton.setAlignment(javafx.geometry.Pos.CENTER);
    }


    private void setupContinueButtonAnimations() {
        // Hover effect for continue button
        continueButton.setOnMouseEntered(e -> {
            String hoverStyle = """
                -fx-background-color: linear-gradient(to bottom, #BA68C8 0%, #9C27B0 50%, #7B1FA2 100%);
                -fx-background-radius: 20px;
                -fx-border-color: #4A148C;
                -fx-border-width: 3px;
                -fx-border-radius: 20px;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 18, 0.5, 0, 6);
                -fx-cursor: hand;
                -fx-font-size: 16px;
                """;
            continueButton.setStyle(hoverStyle);

            // Scale up animation
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), continueButton);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();

            // Glow effect
            Timeline glowEffect = new Timeline();
            glowEffect.setCycleCount(Timeline.INDEFINITE);
            glowEffect.setAutoReverse(true);

            KeyValue glowValue = new KeyValue(continueButton.opacityProperty(), 0.8);
            KeyFrame glowFrame = new KeyFrame(Duration.millis(800), glowValue);
            glowEffect.getKeyFrames().add(glowFrame);
            glowEffect.play();

            continueButton.setUserData(glowEffect); // Store for cleanup
        });

        continueButton.setOnMouseExited(e -> {
            setupContinueButtonStyles(); // Reset to original style

            // Scale down animation
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), continueButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();

            // Stop glow effect
            Timeline glowEffect = (Timeline) continueButton.getUserData();
            if (glowEffect != null) {
                glowEffect.stop();
            }
            continueButton.setOpacity(1.0);
        });

        // Press effect
        continueButton.setOnMousePressed(e -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), continueButton);
            scalePress.setToX(0.95);
            scalePress.setToY(0.95);
            scalePress.play();
        });

        continueButton.setOnMouseReleased(e -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), continueButton);
            scaleRelease.setToX(1.05);
            scaleRelease.setToY(1.05);
            scaleRelease.play();
        });
    }

    private void createLoadingLabel() {
        // Create loading label programmatically
        loadingLabel = new Label("LOADING");
        loadingLabel.setLayoutX(400); // Center horizontally (assuming 900px width)
        loadingLabel.setLayoutY(50);  // Top position
        loadingLabel.setTextFill(Color.WHITE);
        loadingLabel.setFont(Font.font("Lucida Console", 24));
        loadingLabel.setVisible(false);

        // Add to the same parent as other components
        // Get the parent pane and add loading label
        if (storyContainer.getParent() instanceof javafx.scene.layout.Pane) {
            ((javafx.scene.layout.Pane) storyContainer.getParent()).getChildren().add(loadingLabel);
        }
    }

    private void loadRunningFrames() {
        runningFrames = new ArrayList<>();
        try {
            // Load all 6 running frames in order
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run0.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run1.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run2.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run3.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run4.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run5.png")));
        } catch (Exception e) {
            System.out.println("Error loading animation frames: " + e.getMessage());
        }
    }

    @FXML
    public void handleContinue(ActionEvent event) {
        // Add button press effect before transitioning
        ScaleTransition finalPress = new ScaleTransition(Duration.millis(150), continueButton);
        finalPress.setToX(0.9);
        finalPress.setToY(0.9);
        finalPress.setOnFinished(e -> startTransitionAnimation());
        finalPress.play();
    }

    private void startTransitionAnimation() {
        // Create fade out animation for text and continue button only
        FadeTransition fadeText = new FadeTransition(Duration.millis(1000), storyText);
        fadeText.setFromValue(1.0);
        fadeText.setToValue(0.0);

        FadeTransition fadeContinue = new FadeTransition(Duration.millis(1000), continueButton);
        fadeContinue.setFromValue(1.0);
        fadeContinue.setToValue(0.0);

        // Add scale out animation for continue button
        ScaleTransition scaleOutButton = new ScaleTransition(Duration.millis(1000), continueButton);
        scaleOutButton.setToX(0.8);
        scaleOutButton.setToY(0.8);

        // Play fade animations simultaneously
        ParallelTransition fadeOut = new ParallelTransition(fadeText, fadeContinue, scaleOutButton);

        // When fade out is complete, start character animation
        fadeOut.setOnFinished(e -> startCharacterAnimation());
        fadeOut.play();
    }

    private void startCharacterAnimation() {
        // Show loading label first
        showLoadingText();

        // Show character at bottom-left position
        characterImage.setVisible(true);
        characterImage.setLayoutX(0); // Left position
        characterImage.setLayoutY(400); // Bottom position
        characterImage.setFitWidth(80); // Set character size
        characterImage.setFitHeight(80);
        characterImage.setPreserveRatio(true);

        // Set initial frame
        if (!runningFrames.isEmpty()) {
            characterImage.setImage(runningFrames.get(0));
        }

        // Create sprite animation (frame cycling)
        createSpriteAnimation();

        // Create translate animation from left to right
        TranslateTransition moveCharacter = new TranslateTransition(Duration.millis(3500), characterImage);
        moveCharacter.setFromX(0);
        moveCharacter.setToX(900); // Character goes off-screen

        // Create fade out animation for character
        FadeTransition fadeCharacter = new FadeTransition(Duration.millis(1000), characterImage);
        fadeCharacter.setFromValue(1.0);
        fadeCharacter.setToValue(0.0);
        fadeCharacter.setDelay(Duration.millis(2500)); // Start fading when character is near the end

        // Start sprite animation
        spriteAnimation.play();

        // Play character movement and fade simultaneously
        ParallelTransition characterAnimation = new ParallelTransition(moveCharacter, fadeCharacter);

        // When character animation is complete, stop animations and switch to level 1
        characterAnimation.setOnFinished(e -> {
            spriteAnimation.stop();
            if (loadingAnimation != null) {
                loadingAnimation.stop();
            }
            SceneManager.getInstance().switchToScene("level1.fxml");
        });
        characterAnimation.play();
    }

    private void showLoadingText() {
        if (loadingLabel != null) {
            // Make loading label visible
            loadingLabel.setVisible(true);
            loadingLabel.setText("LOADING");

            // Create animated loading text with dots
            loadingAnimation = new Timeline();
            loadingAnimation.setCycleCount(Timeline.INDEFINITE);

            // Create keyframes for loading animation
            KeyFrame frame1 = new KeyFrame(Duration.millis(0), e -> loadingLabel.setText("LOADING"));
            KeyFrame frame2 = new KeyFrame(Duration.millis(500), e -> loadingLabel.setText("LOADING."));
            KeyFrame frame3 = new KeyFrame(Duration.millis(1000), e -> loadingLabel.setText("LOADING.."));
            KeyFrame frame4 = new KeyFrame(Duration.millis(1500), e -> loadingLabel.setText("LOADING..."));

            loadingAnimation.getKeyFrames().addAll(frame1, frame2, frame3, frame4);
            loadingAnimation.play();

            // Fade in loading text
            FadeTransition fadeInLoading = new FadeTransition(Duration.millis(500), loadingLabel);
            fadeInLoading.setFromValue(0.0);
            fadeInLoading.setToValue(1.0);
            fadeInLoading.play();
        }
    }

    private void createSpriteAnimation() {
        if (runningFrames.isEmpty()) return;

        spriteAnimation = new Timeline();
        spriteAnimation.setCycleCount(Timeline.INDEFINITE);

        // Create keyframes for each sprite frame
        Duration frameDuration = Duration.millis(120); // Each frame shows for 120ms
        for (int i = 0; i < runningFrames.size(); i++) {
            final int frameIndex = i;
            KeyFrame keyFrame = new KeyFrame(
                    frameDuration.multiply(i + 1),
                    e -> characterImage.setImage(runningFrames.get(frameIndex))
            );
            spriteAnimation.getKeyFrames().add(keyFrame);
        }

        // Add a keyframe to restart the cycle
        KeyFrame restartFrame = new KeyFrame(
                frameDuration.multiply(runningFrames.size() + 1),
                e -> characterImage.setImage(runningFrames.get(0))
        );
        spriteAnimation.getKeyFrames().add(restartFrame);
    }
}
