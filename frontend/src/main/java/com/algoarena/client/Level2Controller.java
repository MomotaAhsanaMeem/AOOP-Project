package com.algoarena.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.event.ActionEvent;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.*;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Level2Controller {

    @FXML private ImageView characterImage;
    @FXML private Label instructionLabel, levelLabel;
    @FXML private Pane gamePane;

    // Interactive Stack Challenge Components
    @FXML private VBox stackModal;
    @FXML private Label stackQuestionLabel;
    @FXML private VBox elementsPool;
    @FXML private VBox stackArea;
    @FXML private Button checkStackButton, resetStackButton, cancelStackButton;
    @FXML private VBox completionModal;

    // Animation and game state
    private List<Image> runningFrames, idleFrames;
    private Timeline spriteAnimation;
    private boolean isRunning = false;
    private boolean challengeCompleted = false;

    // Enhanced Stack challenge state - More complex operations
    private List<Integer> availableNumbers = Arrays.asList(4, 7, 9, 11, 15, 18, 23, 25, 30);
    private List<Integer> correctStackOrder = Arrays.asList(15, 7); // Bottom to top after all operations
    private List<StackElement> stackElements = new ArrayList<>();

    // Inner class for draggable stack elements
    private class StackElement extends VBox {
        private int value;
        private boolean inStack = false;

        public StackElement(int value) {
            this.value = value;
            this.setAlignment(Pos.CENTER);
            this.setPrefSize(80, 40);
            this.setStyle("-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
                    "-fx-border-color: #2e7d32; -fx-border-width: 2px; -fx-border-radius: 8px; " +
                    "-fx-background-radius: 8px; -fx-cursor: hand;");

            Label valueLabel = new Label(String.valueOf(value));
            valueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Lucida Console';");
            this.getChildren().add(valueLabel);

            setupDragAndDrop();
        }

        private void setupDragAndDrop() {
            this.setOnDragDetected(event -> {
                Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(value));
                db.setContent(content);
                this.setOpacity(0.7);
                event.consume();
            });

            this.setOnDragDone(event -> {
                this.setOpacity(1.0);
                event.consume();
            });
        }

        public int getValue() { return value; }
        public boolean isInStack() { return inStack; }
        public void setInStack(boolean inStack) { this.inStack = inStack; }
    }

    @FXML
    private void initialize() {
        setupScene();
        loadAnimationFrames();
        setupCharacter();
        setupModals();
        setupStackChallenge();
        startIdleAnimation();
    }

    private void setupScene() {
        gamePane.setOpacity(0.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1500), gamePane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        instructionLabel.setText("Welcome to Level 2! Click the character to begin the Advanced Stack Challenge.");
    }

    private void loadAnimationFrames() {
        runningFrames = new ArrayList<>();
        idleFrames = new ArrayList<>();

        try {
            for (int i = 0; i <= 5; i++) {
                runningFrames.add(new Image(getClass().getResourceAsStream("/images/run" + i + ".png")));
            }
            idleFrames.add(new Image(getClass().getResourceAsStream("/images/idle.png")));
            if (!idleFrames.isEmpty()) {
                characterImage.setImage(idleFrames.get(0));
            }
        } catch (Exception e) {
            System.out.println("Error loading animation frames: " + e.getMessage());
        }
    }

    private void setupCharacter() {
        characterImage.setLayoutX(50);
        characterImage.setLayoutY(400);
        characterImage.setOnMouseClicked(this::handleCharacterClick);
    }

    private void setupModals() {
        stackModal.setVisible(false);
        completionModal.setVisible(false);
    }

    private void setupStackChallenge() {
        // Create draggable elements
        for (int num : availableNumbers) {
            StackElement element = new StackElement(num);
            stackElements.add(element);
            elementsPool.getChildren().add(element);
        }

        setupStackDropTarget();
        setupPoolDropTarget();
    }

    private void setupStackDropTarget() {
        stackArea.setOnDragOver(event -> {
            if (event.getGestureSource() != stackArea && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        stackArea.setOnDragEntered(event -> {
            if (event.getGestureSource() != stackArea && event.getDragboard().hasString()) {
                stackArea.setStyle(stackArea.getStyle() + "-fx-background-color: rgba(76, 175, 80, 0.6);");
            }
            event.consume();
        });

        stackArea.setOnDragExited(event -> {
            stackArea.setStyle("-fx-background-color: rgba(0, 50, 100, 0.4); -fx-border-color: #4CAF50; -fx-border-width: 3px; -fx-border-radius: 5px; -fx-padding: 5px;");
            event.consume();
        });

        stackArea.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int value = Integer.parseInt(db.getString());
                StackElement draggedElement = findElementByValue(value);
                if (draggedElement != null) {
                    if (!draggedElement.isInStack()) {
                        elementsPool.getChildren().remove(draggedElement);
                        stackArea.getChildren().add(0, draggedElement);
                        draggedElement.setInStack(true);
                    }
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupPoolDropTarget() {
        elementsPool.setOnDragOver(event -> {
            if (event.getGestureSource() != elementsPool && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        elementsPool.setOnDragEntered(event -> {
            if (event.getGestureSource() != elementsPool && event.getDragboard().hasString()) {
                elementsPool.setStyle(elementsPool.getStyle() + "-fx-background-color: rgba(150, 150, 150, 0.5);");
            }
            event.consume();
        });

        elementsPool.setOnDragExited(event -> {
            elementsPool.setStyle("-fx-background-color: rgba(100, 100, 100, 0.3); -fx-border-color: #888888; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-padding: 10px;");
            event.consume();
        });

        elementsPool.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int value = Integer.parseInt(db.getString());
                StackElement draggedElement = findElementByValue(value);
                if (draggedElement != null && draggedElement.isInStack()) {
                    stackArea.getChildren().remove(draggedElement);
                    elementsPool.getChildren().add(draggedElement);
                    draggedElement.setInStack(false);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private StackElement findElementByValue(int value) {
        return stackElements.stream()
                .filter(element -> element.getValue() == value)
                .findFirst().orElse(null);
    }

    private void startIdleAnimation() {
        if (spriteAnimation != null) spriteAnimation.stop();
        if (!idleFrames.isEmpty()) {
            characterImage.setImage(idleFrames.get(0));
        }
        isRunning = false;
    }

    @FXML
    public void handleCharacterClick(MouseEvent event) {
        if (!isRunning && !challengeCompleted) {
            startRunningToCenter();
            instructionLabel.setText("Running to the advanced challenge area...");
        }
    }

    private void startRunningToCenter() {
        isRunning = true;
        createRunningAnimation();

        double centerX = (900 / 2) - 40;
        double targetTranslateX = centerX - characterImage.getLayoutX();

        TranslateTransition moveToCenter = new TranslateTransition(Duration.millis(3000), characterImage);
        moveToCenter.setFromX(characterImage.getTranslateX());
        moveToCenter.setToX(targetTranslateX);

        spriteAnimation.play();
        moveToCenter.play();

        moveToCenter.setOnFinished(e -> {
            spriteAnimation.stop();
            startIdleAnimation();
            showStackModal();
            instructionLabel.setText("🔥 ADVANCED STACK CHALLENGE! Master these complex operations!");
        });
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
    }

    private void showStackModal() {
        stackModal.setOpacity(0.0);
        stackModal.setVisible(true);
        FadeTransition show = new FadeTransition(Duration.millis(800), stackModal);
        show.setFromValue(0.0);
        show.setToValue(1.0);
        show.play();
    }

    @FXML
    public void handleCheckStack(ActionEvent event) {
        // Get current stack order (from bottom to top)
        List<Integer> currentStack = new ArrayList<>();
        for (int i = stackArea.getChildren().size() - 1; i >= 0; i--) {
            StackElement element = (StackElement) stackArea.getChildren().get(i);
            currentStack.add(element.getValue());
        }

        if (currentStack.equals(correctStackOrder)) {
            instructionLabel.setText("🎉 INCREDIBLE! Advanced stack operations mastered! Final: [15, 7] (bottom to top)");
            hideStackModal();
            challengeCompleted = true;
            completeLevel2();
        } else {
            String userAnswer = currentStack.toString();
            instructionLabel.setText("❌ Not correct! Your answer: " + userAnswer + ". Trace each operation step-by-step!");

            // Enhanced visual feedback
            stackArea.setStyle("-fx-background-color: rgba(255, 50, 50, 0.5); -fx-border-color: #f44336; -fx-border-width: 4px; -fx-border-radius: 5px; -fx-padding: 5px;");

            // Shake animation for incorrect answer
            TranslateTransition shake = new TranslateTransition(Duration.millis(50), stackArea);
            shake.setFromX(0);
            shake.setToX(5);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();

            // Reset style after feedback
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> stackArea.setStyle("-fx-background-color: rgba(0, 50, 100, 0.4); -fx-border-color: #4CAF50; -fx-border-width: 3px; -fx-border-radius: 5px; -fx-padding: 5px;"));
            pause.play();
        }
    }

    @FXML
    public void handleResetStack(ActionEvent event) {
        List<StackElement> elementsInStack = new ArrayList<>();
        for (int i = stackArea.getChildren().size() - 1; i >= 0; i--) {
            elementsInStack.add((StackElement) stackArea.getChildren().get(i));
        }

        stackArea.getChildren().clear();
        for (StackElement element : elementsInStack) {
            element.setInStack(false);
            if (!elementsPool.getChildren().contains(element)) {
                elementsPool.getChildren().add(element);
            }
        }

        instructionLabel.setText("🔄 Stack reset! Trace through all 12 operations carefully.");
    }

    @FXML
    public void handleCancelStack(ActionEvent event) {
        hideStackModal();
        instructionLabel.setText("Challenge cancelled. Click the character to try the advanced stack challenge again.");
        characterImage.setTranslateX(0);
        isRunning = false;
    }

    private void hideStackModal() {
        FadeTransition hide = new FadeTransition(Duration.millis(500), stackModal);
        hide.setFromValue(1.0);
        hide.setToValue(0.0);
        hide.setOnFinished(e -> stackModal.setVisible(false));
        hide.play();
    }

    private void completeLevel2() {
        instructionLabel.setText("🏆 PHENOMENAL! Advanced stack mastery achieved! Ready for Level 3!");

        isRunning = true;
        createRunningAnimation();
        spriteAnimation.play();

        TranslateTransition finalRun = new TranslateTransition(Duration.millis(2500), characterImage);
        finalRun.setFromX(characterImage.getTranslateX());
        finalRun.setToX(characterImage.getTranslateX() + 300);

        finalRun.play();
        finalRun.setOnFinished(e -> {
            spriteAnimation.stop();
            startIdleAnimation();
            showCompletionModal();
        });
    }

    private void showCompletionModal() {
        completionModal.setOpacity(0.0);
        completionModal.setVisible(true);
        FadeTransition show = new FadeTransition(Duration.millis(1000), completionModal);
        show.setFromValue(0.0);
        show.setToValue(1.0);
        show.play();
    }

    @FXML
    public void handleProceedToLevel3(ActionEvent event) {
        FadeTransition sceneTransition = new FadeTransition(Duration.millis(1000), gamePane);
        sceneTransition.setFromValue(1.0);
        sceneTransition.setToValue(0.0);
        sceneTransition.setOnFinished(e -> SceneManager.getInstance().switchToScene("level3.fxml"));
        sceneTransition.play();
    }

    @FXML
    public void handleBackToMenu(ActionEvent event) {
        SceneManager.getInstance().switchToScene("homescreen.fxml");
    }
}
