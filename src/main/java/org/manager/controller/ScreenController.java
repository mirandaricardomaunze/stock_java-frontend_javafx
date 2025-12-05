package org.manager.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ScreenController {
    private final StackPane container;

    public ScreenController(StackPane container) {
        this.container = container;
    }

    public void setScreen(String fxml, String title) {
        try {

           Node node= FXMLLoader.load(getClass().getResource(fxml));
           container.getChildren().setAll(node);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
