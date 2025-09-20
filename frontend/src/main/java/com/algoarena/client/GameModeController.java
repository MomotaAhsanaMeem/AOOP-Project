package com.algoarena.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class GameModeController {

    @FXML
    private Button singlePlayerButton;

    @FXML
    private Button buddyOpButton;

    @FXML
    private Button backButton;

    @FXML
    public void handleSinglePlayer(ActionEvent event) {
        SceneManager.getInstance().switchToScene("gameid.fxml");
    }

    @FXML
    public void handleBuddyOp(ActionEvent event) {
        SceneManager.getInstance().switchToScene("gameid.fxml");
    }

    @FXML
    public void handleBack(ActionEvent event) {
        SceneManager.getInstance().switchToScene("homescreen.fxml");
    }

    @FXML
    private void initialize() {
        System.out.println("Game Mode Selection loaded!");
    }
}
