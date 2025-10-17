package com.algoarena.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;

public class Level3Controller {

    @FXML private StackPane root;

    @FXML private ImageView backgroundImage;
    @FXML private ImageView coreImage;
    @FXML private ImageView playerCharacter;

    @FXML private HBox panels;
    @FXML private VBox cellsPanel;
    @FXML private VBox cellsList;
    @FXML private VBox dropZone;

    @FXML private Label statusLabel;
    @FXML private Label warningLabel;
    @FXML private Label narrativeLabel;

    @FXML private Button activateButton;
    @FXML private Button resetButton;

    @FXML private VBox introModal;
    @FXML private Label introBody;

    // Mini question area
    @FXML private VBox miniQuestion;
    @FXML private Label miniBody;

    // Credits modal
    @FXML private VBox creditsModal;

    // State
    private boolean arrived = false;
    private double stopY;

    // Constraints
    private static final int MAX_WEIGHT = 10;

    // Catalog: A(10,5), B(6,3), C(12,6), D(8,5)
    private final List<Cell> catalog = List.of(
            new Cell("Alpha Cell (A)", "A", 10, 5),
            new Cell("Beta Cell (B)", "B", 6, 3),
            new Cell("Core Cell (C)", "C", 12, 6),
            new Cell("Delta Cell (D)", "D", 8, 5)
    );

    private final List<Cell> selected = new ArrayList<>();
    private final List<String> pickOrder = new ArrayList<>();

    private int totalPower = 0;
    private int totalWeight = 0;

    private static final DataFormat CELL_FORMAT = new DataFormat("algoarena/cell");
    private Timeline runCycle; // cycles run0..run5

    @FXML
    private void initialize() {
        setImage(backgroundImage, "/images/bg3.png");
        setImage(coreImage, "/images/energy_core.png");
        coreImage.setVisible(false);

        // Character idle
        setImage(playerCharacter, "/images/idle.png");
        playerCharacter.setTranslateX(-360);
        playerCharacter.setTranslateY(160);
        stopY = playerCharacter.getTranslateY();

        narrativeLabel.setText("Central Energy Chamber online. Click your hero to approach the Core.");

        // Intro modal hidden
        introModal.setVisible(false);
        introModal.setOpacity(0);

        // Mini question hidden until solving view is open
        miniQuestion.setVisible(false);
        miniQuestion.setOpacity(0);

        // Credits hidden
        creditsModal.setVisible(false);
        creditsModal.setOpacity(0);

        // Solving frame hidden
        panels.setOpacity(0);
        panels.setMouseTransparent(true);

        // Build left panel
        buildCellsUI();
        updateStatus();

        // Problem statement + sample table
        String story =
                "Power the Energy Core\n\n" +
                        "You’ve reached the Central Energy Chamber, the heart of the futuristic city.\n" +
                        "The Core needs 15 energy units to activate, and your hoverpack can only carry 10 weight units.\n\n" +
                        "Your task: Drag and drop the most efficient cells into the Core to reach maximum power without overloading.\n" +
                        "Hint: Think like a greedy algorithm — pick the best power-to-weight ratio first.\n\n" +
                        "Sample question:\n" +
                        "Cell | Value | Weight\n" +
                        "A    | 10    | 5\n" +
                        "B    | 6     | 3\n" +
                        "C    | 12    | 6\n" +
                        "D    | 8     | 5\n\n" +
                        "Target: Reach at least 15 power within 10 weight.\n" +
                        "Optimal here: C + B → power 18, weight 9.";
        introBody.setText(story);
        miniBody.setText(story); // same text mirrored in the mini card
    }

    // ---------------- Character and intro ----------------
    @FXML
    private void handleCharacterClicked() {
        if (arrived) return;
        arrived = true;

        startRunCycle();

        TranslateTransition run = new TranslateTransition(Duration.seconds(2.0), playerCharacter);
        run.setToX(0);
        run.setToY(stopY);
        run.setInterpolator(Interpolator.EASE_BOTH);

        run.setOnFinished(e -> {
            stopRunCycle();
            setImage(playerCharacter, "/images/idle.png");

            // Show the energy core when arriving; intro will pop over it
            coreImage.setVisible(true);
            coreImage.setOpacity(1.0);
            setImage(coreImage, "/images/energy_core.png");

            // Fade the hero only, then open intro
            FadeTransition heroFade = fade(playerCharacter, 1, 0, 500);
            heroFade.setOnFinished(ev -> {
                playerCharacter.setVisible(false);
                showIntroModal();
            });
            heroFade.play();
        });

        run.play();
    }

    private void showIntroModal() {
        introModal.setVisible(true);
        introModal.setOpacity(0);
        fade(introModal, 0, 1, 300).play();
        narrativeLabel.setText("Read the briefing, then click Solve to open the Core container.");
    }

    @FXML
    private void handleIntroSolve() {
        SequentialTransition seq = new SequentialTransition(
                fade(introModal, 1, 0, 200),
                new PauseTransition(Duration.millis(40))
        );
        seq.setOnFinished(e -> {
            introModal.setVisible(false);
            panels.setMouseTransparent(false);
            fade(panels, 0, 1, 300).play();
            narrativeLabel.setText("Drag cells into the container. Limit: Weight ≤ 10. Maximize Power.");

            // Show mini question card for re-reading
            miniQuestion.setVisible(true);
            fade(miniQuestion, 0, 1, 250).play();

            coreImage.setVisible(true); // visible during solving
        });
        seq.play();
    }

    @FXML
    private void handleIntroClose() {
        FadeTransition ft = fade(introModal, 1, 0, 200);
        ft.setOnFinished(e -> introModal.setVisible(false));
        ft.play();
    }

    // ---------------- DnD and selection ----------------
    private void buildCellsUI() {
        cellsList.getChildren().clear();
        for (Cell cell : catalog) {
            VBox card = createCellCard(cell);
            cellsList.getChildren().add(card);
        }
    }

    private VBox createCellCard(Cell cell) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #8fe1ff; -fx-border-radius: 8;");
        Label name = new Label(cell.fullName + " [" + cell.shortName + "]");
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        Label pw = new Label("Power: " + cell.power + "  Weight: " + cell.weight);
        pw.setStyle("-fx-text-fill: #e0f7ff; -fx-font-size: 12; -fx-font-family: 'Lucida Console';");
        card.getChildren().addAll(name, pw);
        card.setUserData(cell);

        // Drag
        card.setOnDragDetected(e -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.put(CELL_FORMAT, cell.shortName); // "A","B","C","D"
            db.setContent(cc);
            card.setOpacity(0.6);
            e.consume();
        });
        card.setOnDragDone(e -> {
            card.setOpacity(1.0);
            e.consume();
        });
        return card;
    }

    @FXML
    private void handleRootDragOver(DragEvent e) {
        if (e.getDragboard().hasContent(CELL_FORMAT)) {
            e.acceptTransferModes(TransferMode.MOVE);
        }
        e.consume();
    }

    @FXML
    private void handleRootDragDropped(DragEvent e) {
        e.setDropCompleted(false);
        e.consume();
    }

    @FXML
    private void handleDropZoneDragOver(DragEvent e) {
        if (e.getDragboard().hasContent(CELL_FORMAT)) {
            e.acceptTransferModes(TransferMode.MOVE);
            highlightDropZone(true);
        }
        e.consume();
    }

    @FXML
    private void handleDropZoneDragExited(DragEvent e) {
        highlightDropZone(false);
        e.consume();
    }

    @FXML
    private void handleDropZoneDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasContent(CELL_FORMAT)) {
            String id = (String) db.getContent(CELL_FORMAT);
            Optional<Cell> item = catalog.stream().filter(c -> c.shortName.equals(id)).findFirst();
            if (item.isPresent() && selected.stream().noneMatch(c -> c.shortName.equals(id))) {
                addSelection(item.get());
                Node pill = buildSelectionPill(item.get());
                dropZone.getChildren().add(pill);
                success = true;
            }
        }
        e.setDropCompleted(success);
        highlightDropZone(false);
        e.consume();
    }

    private Node buildSelectionPill(Cell cell) {
        HBox pill = new HBox(8);
        pill.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-padding: 8; -fx-background-radius: 16; -fx-border-color: #9fe6ff; -fx-border-radius: 16;");
        Label lbl = new Label(cell.shortName + "  P:" + cell.power + "  W:" + cell.weight);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 12; -fx-font-family: 'Lucida Console';");
        Button x = new Button("×");
        x.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff8c8c; -fx-font-weight: bold;");
        x.setOnAction(e -> {
            removeSelection(cell);
            dropZone.getChildren().remove(pill);
        });
        pill.getChildren().addAll(lbl, x);
        return pill;
    }

    private void addSelection(Cell c) {
        selected.add(c);
        pickOrder.add(c.shortName); // record order
        totalPower += c.power;
        totalWeight += c.weight;
        updateStatus();
        validateWeight();
    }

    private void removeSelection(Cell c) {
        if (selected.removeIf(s -> s.shortName.equals(c.shortName))) {
            for (int i = pickOrder.size() - 1; i >= 0; i--) {
                if (pickOrder.get(i).equals(c.shortName)) { pickOrder.remove(i); break; }
            }
            totalPower -= c.power;
            totalWeight -= c.weight;
            updateStatus();
            validateWeight();
        }
    }

    private void updateStatus() {
        statusLabel.setText(String.format("Total Weight: %d | Total Power: %d", totalWeight, totalPower));
    }

    private void validateWeight() {
        if (totalWeight > MAX_WEIGHT) {
            warningLabel.setText("Overweight! The Core is destabilizing!");
            shake(dropZone);
        } else {
            warningLabel.setText("");
        }
    }

    // ---------------- Controls ----------------
    @FXML
    private void handleReset() {
        selected.clear();
        pickOrder.clear();
        totalPower = 0;
        totalWeight = 0;
        dropZone.getChildren().clear();
        updateStatus();
        warningLabel.setText("");
        narrativeLabel.setText("Reset complete. Try a different combination.");
    }

    @FXML
    private void handleActivate() {
        if (totalWeight > MAX_WEIGHT) {
            warningLabel.setText("Overweight! The Core is destabilizing!");
            shake(dropZone);
            return;
        }

        // Strict requirement: exactly two picks, in order C then B
        boolean correctSequence = pickOrder.size() == 2
                && "C".equals(pickOrder.get(0))
                && "B".equals(pickOrder.get(1));

        if (correctSequence) {
            narrativeLabel.setText("Correct sequence detected. The Core activates!");
            glowCore(true);

            SequentialTransition seq = new SequentialTransition(
                    new PauseTransition(Duration.seconds(1.0)),
                    fade(panels, panels.getOpacity(), 0, 350),
                    fade(miniQuestion, miniQuestion.getOpacity(), 0, 250)
            );
            seq.setOnFinished(xx -> {
                panels.setMouseTransparent(true);
                panels.setOpacity(0);
                miniQuestion.setVisible(false);
                exitAfterSuccess();
            });
            seq.play();
        } else {
            narrativeLabel.setText("Incorrect. Use the best ratio first: pick C, then B.");
            glowCore(false);
            shake(dropZone);
        }
    }

    // ---------------- Finale ----------------
    private void exitAfterSuccess() {
        playerCharacter.setVisible(true);
        playerCharacter.setOpacity(1);
        playerCharacter.setTranslateX(0);
        playerCharacter.setTranslateY(stopY);

        setImage(playerCharacter, "/images/run0.png");
        startRunCycle();

        TranslateTransition runOut = new TranslateTransition(Duration.seconds(2.2), playerCharacter);
        runOut.setToX(480);
        runOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition f = fade(playerCharacter, 1, 0, 1200);
        f.setDelay(Duration.seconds(0.9));

        ParallelTransition out = new ParallelTransition(runOut, f);
        out.setOnFinished(done -> {
            stopRunCycle();
            showFinalMessageThenCredits();
        });
        out.play();
    }

    private void showFinalMessageThenCredits() {
        Label finale = new Label("You successfully completed the level.\nYou finally return to your original future.");
        finale.setStyle("-fx-text-fill: #e4f7ff; -fx-font-size: 22; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, #00d4ff, 12, 0.25, 0, 0);");
        root.getChildren().add(finale);
        finale.setOpacity(0);

        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(finale.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(900), new KeyValue(finale.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(2.2), new KeyValue(finale.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(3.4), new KeyValue(finale.opacityProperty(), 0))
        );
        t.setOnFinished(e -> {
            root.getChildren().remove(finale);
            showCreditsModal();
        });
        t.play();
    }

    private void showCreditsModal() {
        creditsModal.setVisible(true);
        creditsModal.setOpacity(0);
        fade(creditsModal, 0, 1, 350).play();
    }

    @FXML
    private void handleReturnHome() {
        // Fade to black (credits already dark), then load HomePage
        SequentialTransition seq = new SequentialTransition(
                fade(creditsModal, 1, 0, 250)
        );
        seq.setOnFinished(e -> {
            creditsModal.setVisible(false);
            goHome();
        });
        seq.play();
    }

    private void goHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homescreen.fxml"));
            Parent home = loader.load();
            // Expecting HomePageController as controller in the FXML
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(new Scene(home));
        } catch (IOException ex) {
            System.err.println("Failed to load Home page: " + ex.getMessage());
        }
    }

    // ---------------- Visual helpers ----------------
    private void setImage(ImageView view, String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                view.setImage(new Image(is));
            } else {
                System.err.println("Resource not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Failed to load: " + path + " -> " + e.getMessage());
        }
    }

    private FadeTransition fade(Node n, double from, double to, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), n);
        ft.setFromValue(from);
        ft.setToValue(to);
        return ft;
    }

    private void highlightDropZone(boolean on) {
        dropZone.setEffect(on ? new DropShadow(20, Color.CYAN) : null);
    }

    private void shake(Node n) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(80), n);
        tt.setFromX(-5);
        tt.setToX(5);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void glowCore(boolean success) {
        Color color = success ? Color.CYAN : Color.ORANGERED;
        DropShadow ds = new DropShadow(0, color);
        coreImage.setEffect(ds);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(ds.radiusProperty(), 0)),
                new KeyFrame(Duration.millis(450), new KeyValue(ds.radiusProperty(), 36)),
                new KeyFrame(Duration.millis(850), new KeyValue(ds.radiusProperty(), 0))
        );
        tl.play();
    }

    private void startRunCycle() {
        stopRunCycle();
        final String[] frames = { "/images/run0.png","/images/run1.png","/images/run2.png","/images/run3.png","/images/run4.png","/images/run5.png" };
        final int[] idx = {0};
        runCycle = new Timeline(new KeyFrame(Duration.millis(90), e -> {
            setImage(playerCharacter, frames[idx[0]]);
            idx[0] = (idx[0] + 1) % frames.length;
        }));
        runCycle.setCycleCount(Animation.INDEFINITE);
        runCycle.play();
    }

    private void stopRunCycle() {
        if (runCycle != null) {
            runCycle.stop();
            runCycle = null;
        }
    }

    // Data model
    private static class Cell {
        final String fullName, shortName;
        final int power, weight;
        Cell(String fullName, String shortName, int power, int weight) {
            this.fullName = fullName;
            this.shortName = shortName;
            this.power = power;
            this.weight = weight;
        }
        double ratio() { return weight == 0 ? 0 : (double) power / weight; }
    }
}
