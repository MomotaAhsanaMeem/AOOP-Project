package com.algoarena.client;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import com.algoarena.client.net.AppContext;
import com.algoarena.client.net.MessageListener;

public class Level1Controller {

    @FXML private ImageView characterImage;
    @FXML private ImageView rockObstacle1;
    @FXML private ImageView rockObstacle2;
    @FXML private ImageView rockObstacle3;
    @FXML private Label instructionLabel;
    @FXML private Pane gamePane;

    // Quiz modal components
    @FXML private VBox quizModal;
    @FXML private Label questionLabel;
    @FXML private RadioButton optionA;
    @FXML private RadioButton optionB;
    @FXML private RadioButton optionC;
    @FXML private RadioButton optionD;
    @FXML private ToggleGroup answerGroup;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    // Guide UI (sidebar)
    @FXML private Button guideButton;
    @FXML private VBox guidePane;
    @FXML private TextArea guideChatArea;
    @FXML private TextField guideInput;

    // Animation variables
    private List<Image> runningFrames;
    private List<Image> idleFrames;
    private Timeline spriteAnimation;
    private boolean isRunning = false;
    private int currentObstacle = 0; // 0,1,2
    private final boolean[] obstaclesSolved = {false, false, false};

    // Current question state
    private String currentQuestionId = null;
    private final List<String> currentOptions = new ArrayList<>();

    @FXML
    private void initialize() {
        instructionLabel.setText("Click on the character to start running!");
        loadAnimationFrames();

        // Character base position & size
        characterImage.setLayoutX(34);
        characterImage.setLayoutY(429);
        characterImage.setFitWidth(80);
        characterImage.setFitHeight(80);
        characterImage.setPreserveRatio(true);

        // Rock positions
        rockObstacle1.setLayoutX(228); rockObstacle1.setLayoutY(432);
        rockObstacle1.setFitWidth(100); rockObstacle1.setFitHeight(120); rockObstacle1.setPreserveRatio(true);
        rockObstacle2.setLayoutX(450); rockObstacle2.setLayoutY(419);
        rockObstacle2.setFitWidth(100); rockObstacle2.setFitHeight(120); rockObstacle2.setPreserveRatio(true);
        rockObstacle3.setLayoutX(674); rockObstacle3.setLayoutY(448);
        rockObstacle3.setFitWidth(100); rockObstacle3.setFitHeight(120); rockObstacle3.setPreserveRatio(true);

        // Hide quiz initially
        quizModal.setVisible(false);

        // Toggle group for options (if FXML didn’t already link)
        if (answerGroup == null) {
            answerGroup = new ToggleGroup();
        }
        optionA.setToggleGroup(answerGroup);
        optionB.setToggleGroup(answerGroup);
        optionC.setToggleGroup(answerGroup);
        optionD.setToggleGroup(answerGroup);

        // Character click starts movement
        characterImage.setOnMouseClicked(this::handleCharacterClick);

        // Idle animation to show sprite
        startIdleAnimation();

        // Guide pane hidden by default (if present)
        if (guidePane != null) guidePane.setVisible(false);

        // --- Socket listener wiring ---
        // Assumes your AppContext.net() is already connected from the name screen.
        AppContext.net().setListener(new MessageListener() {
            // QUESTION frame → populate UI
            @Override
            public void onQuestion(String questionId, String text, List<String> options, int timeLimitMs) {
                Platform.runLater(() -> populateQuestion(questionId, text, options));
            }

            // ANSWER_EVAL frame → proceed or retry
            @Override
            public void onAnswerEval(String qid, boolean isCorrect, int correctIndex, int deltaPoints, int totalPoints) {
                Platform.runLater(() -> {
                    if (isCorrect) {
                        instructionLabel.setText("Correct! +" + deltaPoints);
                        quizModal.setVisible(false);
                        if (currentObstacle >= 0 && currentObstacle < obstaclesSolved.length) {
                            obstaclesSolved[currentObstacle] = true;
                            currentObstacle++;
                        }
                        continueAfterObstacle();
                    } else {
                        instructionLabel.setText("Not quite. " +
                                (correctIndex >= 0 ? "Think again using the hint." : "Try a different option."));
                        answerGroup.selectToggle(null);
                    }
                });
            }

            @Override public void onScore(int totalPoints) {
                // Hook to a score label if you add one
            }

            @Override public void onProgress(int moveBy, int progress) {
                // Optional: server-driven movement (not required for this level)
            }

            @Override
            public void onError(String code, String message) {
                Platform.runLater(() -> instructionLabel.setText("Server error: " + code + " - " + message));
            }

            // Guide streaming
            @Override public void onChatStart(String id) {
                if (guideChatArea != null) Platform.runLater(() -> guideChatArea.appendText("Guide: "));
            }
            @Override public void onChatDelta(String id, String chunk) {
                if (guideChatArea != null) Platform.runLater(() -> guideChatArea.appendText(chunk));
            }
            @Override public void onChatEnd(String id) {
                if (guideChatArea != null) Platform.runLater(() -> guideChatArea.appendText("\n"));
            }
        });
    }

    // ---------- Animations ----------

    private void loadAnimationFrames() {
        runningFrames = new ArrayList<>();
        idleFrames = new ArrayList<>();
        try {
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run0.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run1.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run2.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run3.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run4.png")));
            runningFrames.add(new Image(getClass().getResourceAsStream("/images/run5.png")));
            idleFrames.add(new Image(getClass().getResourceAsStream("/images/idle.png")));

            if (!idleFrames.isEmpty()) {
                characterImage.setImage(idleFrames.get(0));
            }
        } catch (Exception e) {
            System.out.println("Error loading animation frames: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startIdleAnimation() {
        if (spriteAnimation != null) spriteAnimation.stop();
        if (!idleFrames.isEmpty()) characterImage.setImage(idleFrames.get(0));
        isRunning = false;
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

    // ---------- Game flow ----------

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
        createRunningAnimation();

        double targetX = getTargetPosition(currentObstacle);

        TranslateTransition moveToRock = new TranslateTransition(Duration.millis(2000), characterImage);
        moveToRock.setFromX(characterImage.getTranslateX());
        moveToRock.setToX(targetX);

        spriteAnimation.play();
        moveToRock.play();

        moveToRock.setOnFinished(e -> {
            spriteAnimation.stop();
            startIdleAnimation();

            // Ask backend for a fresh question instead of hardcoded ones
            requestQuestionFromServer();
            instructionLabel.setText("Loading a fresh question...");
        });
    }

    private double getTargetPosition(int obstacleIndex) {
        return switch (obstacleIndex) {
            case 0 -> 150;  // stop before first rock
            case 1 -> 372;  // stop before second rock
            case 2 -> 594;  // stop before third rock
            default -> 750; // end
        };
    }

    private void continueAfterObstacle() {
        if (currentObstacle >= 3) {
            runToEndAndFade();
            return;
        }

        isRunning = true;
        createRunningAnimation();

        double nextPosition = getTargetPosition(currentObstacle);
        TranslateTransition moveToNext = new TranslateTransition(Duration.millis(1500), characterImage);
        moveToNext.setFromX(characterImage.getTranslateX());
        moveToNext.setToX(nextPosition);

        spriteAnimation.play();
        moveToNext.play();

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

        createRunningAnimation();
        spriteAnimation.play();

        TranslateTransition finalRun = new TranslateTransition(Duration.millis(3000), characterImage);
        finalRun.setFromX(characterImage.getTranslateX());
        finalRun.setToX(950);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(1500), characterImage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(1500));

        finalRun.play();
        fadeOut.play();

        finalRun.setOnFinished(e -> {
            spriteAnimation.stop();
            instructionLabel.setText("Level 1 Complete! Proceeding to Level 2...");

            Timeline transitionDelay = new Timeline(new KeyFrame(Duration.millis(2000), evt -> {
                FadeTransition sceneTransition = new FadeTransition(Duration.millis(1000), gamePane);
                sceneTransition.setFromValue(1.0);
                sceneTransition.setToValue(0.0);
                sceneTransition.setOnFinished(transitionEvent ->
                        SceneManager.getInstance().switchToScene("level2.fxml"));
                sceneTransition.play();
            }));
            transitionDelay.play();
        });
    }

    // ---------- Backend wiring (Q&A) ----------

    private void requestQuestionFromServer() {
        // Show modal in "loading" state while waiting for server
        quizModal.setVisible(true);
        questionLabel.setText("Loading question...");
        optionA.setText("...");
        optionB.setText("...");
        optionC.setText("...");
        optionD.setText("...");
        optionA.setVisible(true);
        optionB.setVisible(true);
        optionC.setVisible(true);
        optionD.setVisible(true);
        answerGroup.selectToggle(null);

        // Ask backend
        AppContext.net().requestQuestion();
    }

    private void populateQuestion(String questionId, String text, List<String> options) {
        currentQuestionId = questionId;
        currentOptions.clear();
        if (options != null) currentOptions.addAll(options);

        questionLabel.setText(text != null ? text : "Question");

        // Safely set up to 4 options
        String a = currentOptions.size() > 0 ? currentOptions.get(0) : "";
        String b = currentOptions.size() > 1 ? currentOptions.get(1) : "";
        String c = currentOptions.size() > 2 ? currentOptions.get(2) : "";
        String d = currentOptions.size() > 3 ? currentOptions.get(3) : "";

        optionA.setVisible(!a.isEmpty()); optionA.setManaged(!a.isEmpty()); optionA.setText(a);
        optionB.setVisible(!b.isEmpty()); optionB.setManaged(!b.isEmpty()); optionB.setText(b);
        optionC.setVisible(!c.isEmpty()); optionC.setManaged(!c.isEmpty()); optionC.setText(c);
        optionD.setVisible(!d.isEmpty()); optionD.setManaged(!d.isEmpty()); optionD.setText(d);

        answerGroup.selectToggle(null);
        quizModal.setVisible(true);
        instructionLabel.setText("Solve the coding puzzle to continue!");
    }

    @FXML
    public void handleSubmitAnswer(ActionEvent event) {
        if (currentQuestionId == null) {
            instructionLabel.setText("No question yet—click the character again.");
            return;
        }
        var sel = answerGroup.getSelectedToggle();
        if (sel == null) {
            instructionLabel.setText("Please select an answer first!");
            return;
        }

        int idx = -1;
        if (sel == optionA) idx = 0;
        else if (sel == optionB) idx = 1;
        else if (sel == optionC) idx = 2;
        else if (sel == optionD) idx = 3;

        if (idx < 0) {
            instructionLabel.setText("Please select an answer!");
            return;
        }

        // Send to backend; continueAfterObstacle() happens on ANSWER_EVAL if correct
        AppContext.net().submitAnswer(currentQuestionId, idx);
    }

    @FXML
    public void handleCancelQuiz(ActionEvent event) {
        quizModal.setVisible(false);
        instructionLabel.setText("Challenge cancelled. Click the character to try again.");
        startIdleAnimation();
    }

    // ---------- Guide (chat) ----------

    @FXML
    public void onGuideToggle() {
        if (guidePane == null) return;
        guidePane.setVisible(!guidePane.isVisible());
    }

    @FXML
    public void onGuideSend() {
        if (guideInput == null || guideChatArea == null) return;
        String msg = guideInput.getText() == null ? "" : guideInput.getText().trim();
        if (msg.isEmpty()) return;

        guideChatArea.appendText("You: " + msg + "\n");
        AppContext.net().sendChat(msg);   // sends {"type":"CHAT_USER","data":{"text":msg}}
        guideInput.clear();
    }

    // ---------- Navigation ----------

    @FXML
    public void handleBackToMenu(ActionEvent event) {
        SceneManager.getInstance().switchToScene("homescreen.fxml");
    }

    // ---------- Helpers ----------

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
