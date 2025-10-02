package com.algoarena.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

public class Level1Controller {
    @FXML
    private Label levelTitle;

    @FXML
    private void initialize() {
        levelTitle.setText("Level 1 - The First Challenge");
    }

    @FXML
    public void handleBackToMenu(ActionEvent event) {
        SceneManager.getInstance().switchToScene("homescreen.fxml");
    }
}
