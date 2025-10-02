package com.algoarena.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
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

    // Store animation frames
    private List<Image> runningFrames;
    private Timeline spriteAnimation;

    @FXML
    private void initialize() {
        storyText.setText("While experimenting with an ancient device, a gifted young coder triggers a strange anomaly and is suddenly hurled into a desolate, apocalyptic future. As he navigates this ruined world, he encounters a brilliant professor who reveals the existence of a powerful machine—one that could, in theory, send him back to the past, allowing him to correct the anomaly and restore the world's future.\n" +
                "\n" +
                "The professor warns him that the path will not be simple. To restore the machine, he must journey through ***three perilous domains***—each holding a vital piece required to bring the device back to life. Though the trials ahead are daunting, this is no impossible task for the young coder. For where others falter, his sharp logic and unyielding reasoning give him the edge to face every challenge that awaits.");
        storyText.setFont(Font.font("Lucida Console", 18));

        // Initially hide the character image
        characterImage.setVisible(false);

        // Load running animation frames
        loadRunningFrames();
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
        startTransitionAnimation();
    }

    private void startTransitionAnimation() {
        // Create fade out animation for text and continue button only
        FadeTransition fadeText = new FadeTransition(Duration.millis(1000), storyText);
        fadeText.setFromValue(1.0);
        fadeText.setToValue(0.0);

        FadeTransition fadeContinue = new FadeTransition(Duration.millis(1000), continueButton);
        fadeContinue.setFromValue(1.0);
        fadeContinue.setToValue(0.0);

        // Play fade animations simultaneously
        ParallelTransition fadeOut = new ParallelTransition(fadeText, fadeContinue);

        // When fade out is complete, start character animation
        fadeOut.setOnFinished(e -> startCharacterAnimation());
        fadeOut.play();
    }

    private void startCharacterAnimation() {
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

        // Create translate animation from left to right (adjusted for 900px width)
        TranslateTransition moveCharacter = new TranslateTransition(Duration.millis(3500), characterImage);
        moveCharacter.setFromX(0);
        moveCharacter.setToX(900); // Adjusted for 900px width (character goes off-screen)

        // Create fade out animation for character
        FadeTransition fadeCharacter = new FadeTransition(Duration.millis(1000), characterImage);
        fadeCharacter.setFromValue(1.0);
        fadeCharacter.setToValue(0.0);
        fadeCharacter.setDelay(Duration.millis(2500)); // Start fading when character is near the end

        // Start sprite animation
        spriteAnimation.play();

        // Play character movement and fade simultaneously
        ParallelTransition characterAnimation = new ParallelTransition(moveCharacter, fadeCharacter);

        // When character animation is complete, stop sprite animation and switch to level 1
        characterAnimation.setOnFinished(e -> {
            spriteAnimation.stop();
            SceneManager.getInstance().switchToScene("level1.fxml");
        });
        characterAnimation.play();
    }

    private void createSpriteAnimation() {
        if (runningFrames.isEmpty()) return;

        spriteAnimation = new Timeline();
        spriteAnimation.setCycleCount(Timeline.INDEFINITE);

        // Create keyframes for each sprite frame
        Duration frameDuration = Duration.millis(120); // Each frame shows for 120ms (about 8 FPS)

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
