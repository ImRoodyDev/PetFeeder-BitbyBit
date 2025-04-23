package com.bitbybit.controllers;

import java.io.IOException;

import com.bitbybit.base.BaseSceneController;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class AppController extends BaseSceneController {
    @FXML
    private AnchorPane appView;

    // This method will be automatically called after the FXML is loaded
    @Override
    public void initialize() {
        super.initialize();
        System.out.println("Initialize FXML controller");
    }

    /**
     * Set the sceneController
     */
    public void setSceneController(SceneController controller) throws IOException {
        // Send scene Controller to parent class
        sceneController = controller;
    }

    /**
     * Switch to login scene
     */
    public void switchToLogin() throws IOException {
        this.sceneController.switchToScene("Login");
    }

    /**
     * Switch to register scene
     */
    public void switchToRegister() throws IOException {
        this.sceneController.switchToScene("Register");
    }
}
