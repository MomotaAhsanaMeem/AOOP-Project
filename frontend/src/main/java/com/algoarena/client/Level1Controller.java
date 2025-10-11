package com.algoarena.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.event.ActionEvent;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Level1Controller {
    @FXML
    private ImageView characterImage;
    @FXML
    private ImageView rockObstacle1;
    @FXML
    private ImageView rockObstacle2;
    @FXML
    private ImageView rockObstacle3;
    @FXML
    private Label instructionLabel;
    @FXML
    private Pane gamePane;

    // Quiz modal components
    @FXML
    private VBox quizModal;
    @FXML
    private Label questionLabel;
    @FXML
    private RadioButton optionA;
    @FXML
    private RadioButton optionB;
    @FXML
    private RadioButton optionC;
    @FXML
    private RadioButton optionD;
    @FXML
    private ToggleGroup answerGroup;
    @FXML
    private Button submitButton;
    @FXML
    private Button cancelButton;

    // Animation variables
    private List<Image> runningFrames;
    private List<Image> idleFrames;
    private Timeline spriteAnimation;
    private boolean isRunning = false;
    private int currentObstacle = 0; // Track which obstacle we're at (0, 1, or 2)
    private boolean[] obstaclesSolved = {false, false, false}; // Track solved obstacles

    @FXML
    private void initialize() {
        instructionLabel.setText("Click on the character to start running!");

        // Load animation frames
        loadAnimationFrames();

        // Set character position aligned to green grass area
        characterImage.setLayoutX(34);
        characterImage.setLayoutY(429);
        characterImage.setFitWidth(80);
        characterImage.setFitHeight(80);
        characterImage.setPreserveRatio(true);

        // Set rock obstacle positions
        rockObstacle1.setLayoutX(228);
        rockObstacle1.setLayoutY(432);
        rockObstacle1.setFitWidth(100);
        rockObstacle1.setFitHeight(120);
        rockObstacle1.setPreserveRatio(true);

        rockObstacle2.setLayoutX(450);
        rockObstacle2.setLayoutY(419);
        rockObstacle2.setFitWidth(100);
        rockObstacle2.setFitHeight(120);
        rockObstacle2.setPreserveRatio(true);

        rockObstacle3.setLayoutX(674);
        rockObstacle3.setLayoutY(448);
        rockObstacle3.setFitWidth(100);
        rockObstacle3.setFitHeight(120);
        rockObstacle3.setPreserveRatio(true);

        // Hide quiz modal initially
        quizModal.setVisible(false);

        // Set up radio button group
        answerGroup = new ToggleGroup();
        optionA.setToggleGroup(answerGroup);
        optionB.setToggleGroup(answerGroup);
        optionC.setToggleGroup(answerGroup);
        optionD.setToggleGroup(answerGroup);

        // Set up character click event
        characterImage.setOnMouseClicked(this::handleCharacterClick);

        // Start idle animation
        startIdleAnimation();
    }

    private void loadAnimationFrames() {
        runningFrames = new ArrayList<>();
        idleFrames = new ArrayList<>();

        try {
            // Load running frames
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run0.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run1.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run2.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run3.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run4.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run5.png")));

            // Load idle frame
            idleFrames.add(new Image(getClass().getResourceAsStream("/images/idle.png")));

            // Set initial idle frame
            if (!idleFrames.isEmpty()) {
                characterImage.setImage(idleFrames.get(0));
            }

        } catch (Exception e) {
            System.out.println("Error loading animation frames: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startIdleAnimation() {
        if (spriteAnimation != null) {
            spriteAnimation.stop();
        }

        characterImage.setImage(idleFrames.get(0));
        isRunning = false;
    }

    @FXML
    public void handleCharacterClick(MouseEvent event) {
        if (!isRunning && currentObstacle < 3) {
            startRunningAnimation();
            instructionLabel.setText("Character is running towards obstacle " + (currentObstacle + 1) + "!");
        } else if (currentObstacle >= 3) {
            instructionLabel.setText("All obstacles completed! Level finished!");
        }
    }

    private void startRunningAnimation() {
        isRunning = true;

        // Create running sprite animation
        createRunningAnimation();

        // Calculate target position based on current obstacle
        double targetX = getTargetPosition(currentObstacle);

        // Create movement animation towards current rock
        TranslateTransition moveToRock = new TranslateTransition(Duration.millis(2000), characterImage);
        moveToRock.setFromX(characterImage.getTranslateX());
        moveToRock.setToX(targetX);

        // Start animations
        spriteAnimation.play();
        moveToRock.play();

        // When movement is complete, stop at obstacle and show quiz
        moveToRock.setOnFinished(e -> {
            spriteAnimation.stop();
            startIdleAnimation();
            showQuizModal();
            instructionLabel.setText("Solve the coding puzzle to continue!");
        });
    }

    private double getTargetPosition(int obstacleIndex) {
        switch (obstacleIndex) {
            case 0: return 150;  // Stop before first rock (228 - 34 - 50)
            case 1: return 372;  // Stop before second rock (450 - 34 - 50)
            case 2: return 594;  // Stop before third rock (672 - 34 - 50)
            default: return 750; // Final position
        }
    }

    private void createRunningAnimation() {
        if (runningFrames.isEmpty()) return;

        spriteAnimation = new Timeline();
        spriteAnimation.setCycleCount(Timeline.INDEFINITE);

        Duration frameDuration = Duration.millis(120);

        for (int i = 0; i < runningFrames.size(); i++) {
            final int frameIndex = i;
            KeyFrame keyFrame = new KeyFrame(
                    frameDuration.multiply(i + 1),
                    e -> characterImage.setImage(runningFrames.get(frameIndex))
            );
            spriteAnimation.getKeyFrames().add(keyFrame);
        }

        KeyFrame restartFrame = new KeyFrame(
                frameDuration.multiply(runningFrames.size() + 1),
                e -> characterImage.setImage(runningFrames.get(0))
        );
        spriteAnimation.getKeyFrames().add(restartFrame);
    }

    private void showQuizModal() {
        // Set question based on current obstacle
        switch (currentObstacle) {
            case 0:
                // Bubble Sort Question
                questionLabel.setText("Array [12, 5, 8, 1]: Which is the result after ONE full pass of bubble sort?");
                optionA.setText("A. [5, 8, 1, 12]");
                optionB.setText("B. [5, 12, 8, 1]");
                optionC.setText("C. [5, 8, 1, 12]"); // Correct
                optionD.setText("D. [1, 5, 8, 12]");
                break;
            case 1:
                // Linear Search Question
                questionLabel.setText("You need to find the dry stick labeled 7 in an unsorted pile [2, 5, 7, 9, 4]. Which algorithm is the most appropriate and simplest?");
                optionA.setText("A. Binary Search");
                optionB.setText("B. Linear Search"); // Correct
                optionC.setText("C. Merge Sort + binary search");
                optionD.setText("D. Dijkstra's algorithm");
                break;
            case 2:
                // Find Minimum Question
                questionLabel.setText("You want to find the smallest stone in [12, 3, 9, 5]. Which sequence of steps is correct?");
                optionA.setText("A. Initialize min = first element → For each element compare → If element < min, set min = element → Return min."); // Correct
                optionB.setText("B. Set min = 0 → For each element multiply → Return min");
                optionC.setText("C. Sort array completely then return first element");
                optionD.setText("D. Initialize min = last element → decrement index → return min");
                break;
        }

        // Clear previous selection
        answerGroup.selectToggle(null);

        // Show modal
        quizModal.setVisible(true);
    }

    @FXML
    public void handleSubmitAnswer(ActionEvent event) {
        RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();

        if (selected == null) {
            instructionLabel.setText("Please select an answer first!");
            return;
        }

        boolean isCorrect = false;
        String correctFeedback = "";

        // Check answer based on current obstacle
        switch (currentObstacle) {
            case 0:
                isCorrect = (selected == optionC);
                correctFeedback = "Correct! The largest element (12) bubbles to the end.";
                break;
            case 1:
                isCorrect = (selected == optionB);
                correctFeedback = "Correct! Linear search works on unsorted arrays.";
                break;
            case 2:
                isCorrect = (selected == optionA);
                correctFeedback = "Correct! This is the standard minimum finding algorithm.";
                break;
        }

        if (isCorrect) {
            // Correct answer
            quizModal.setVisible(false);
            obstaclesSolved[currentObstacle] = true;
            currentObstacle++;
            continueAfterObstacle();
            instructionLabel.setText(correctFeedback + " Moving to next challenge...");
        } else {
            // Wrong answer
            String wrongFeedback = getWrongAnswerFeedback(currentObstacle);
            instructionLabel.setText(wrongFeedback);
            answerGroup.selectToggle(null); // Clear selection
        }
    }

    private String getWrongAnswerFeedback(int obstacleIndex) {
        switch (obstacleIndex) {
            case 0: return "Wrong! Think: bubble sort moves the largest element to the end each pass.";
            case 1: return "Wrong! Binary search requires sorted arrays. Linear search works on any array.";
            case 2: return "Wrong! We need to compare each element with current minimum.";
            default: return "Wrong answer! Try again.";
        }
    }

    @FXML
    public void handleCancelQuiz(ActionEvent event) {
        quizModal.setVisible(false);
        instructionLabel.setText("Challenge cancelled. Click the character to try again.");

        // Don't reset character position, let them try the same obstacle again
        startIdleAnimation();
    }

    private void continueAfterObstacle() {
        if (currentObstacle >= 3) {
            // All obstacles completed - run to end and fade away
            runToEndAndFade();
            return;
        }

        isRunning = true;

        // Create running animation again
        createRunningAnimation();

        // Move character past the current obstacle
        double nextPosition = getTargetPosition(currentObstacle);
        TranslateTransition moveToNext = new TranslateTransition(Duration.millis(1500), characterImage);
        moveToNext.setFromX(characterImage.getTranslateX());
        moveToNext.setToX(nextPosition);

        // Start animations
        spriteAnimation.play();
        moveToNext.play();

        // When movement is complete, prepare for next obstacle
        moveToNext.setOnFinished(e -> {
            spriteAnimation.stop();
            startIdleAnimation();
            if (currentObstacle < 3) {
                instructionLabel.setText("Click the character to continue to obstacle " + (currentObstacle + 1) + "!");
            } else {
                instructionLabel.setText("Level 1 Complete! Well done!");
            }
        });
    }

    private void runToEndAndFade() {
        isRunning = true;
        instructionLabel.setText("Amazing! All challenges completed! The hero continues his journey...");

        // Create running animation for final run
        createRunningAnimation();
        spriteAnimation.play();

        // Create movement animation to run past the end of screen
        TranslateTransition finalRun = new TranslateTransition(Duration.millis(3000), characterImage);
        finalRun.setFromX(characterImage.getTranslateX());
        finalRun.setToX(950); // Run off the right side of the screen (900px + 50px buffer)

        // Create fade out animation that starts during the run
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1500), characterImage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(1500)); // Start fading halfway through the run

        // Start both animations
        finalRun.play();
        fadeOut.play();

        // When animations complete, transition to Level 2
        finalRun.setOnFinished(e -> {
            spriteAnimation.stop();
            instructionLabel.setText("Level 1 Complete! Proceeding to Level 2...");

            // Add a brief pause then transition to Level 2
            Timeline transitionDelay = new Timeline(new KeyFrame(Duration.millis(2000), event -> {
                // Fade out the entire scene before switching
                FadeTransition sceneTransition = new FadeTransition(Duration.millis(1000), gamePane);
                sceneTransition.setFromValue(1.0);
                sceneTransition.setToValue(0.0);

                sceneTransition.setOnFinished(transitionEvent -> {
                    // Switch to Level 2
                    SceneManager.getInstance().switchToScene("level2.fxml");
                });

                sceneTransition.play();
            }));
            transitionDelay.play();
        });
    }


    @FXML
    public void handleBackToMenu(ActionEvent event) {
        SceneManager.getInstance().switchToScene("homescreen.fxml");
    }
}
