package com.bitbybit;

import com.bitbybit.controllers.SceneController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
	/**
	 * Application scene controller
	 */
	SceneController sceneController;

	@Override
	public void start(Stage stage) throws Exception {
		// Create SceneController and pass the Stage explicitly
		sceneController = new SceneController(stage);

		// Initialize screens
		sceneController.initialize(1280, 720);
	}

	@Override
	public void stop() throws Exception {
		sceneController.onClose();
		super.stop();
	}

	// Start the application process
	public static void main(String[] args) throws Exception {
		// Launch the app
		launch();
	}
}
