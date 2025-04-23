package com.bitbybit.controllers;

import java.io.IOException;
import java.util.*;

import com.bitbybit.base.BaseSceneController;
import com.bitbybit.services.authentication.UserManager;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import javafx.stage.StageStyle;

public class SceneController {
	// Primary stage object
	public Stage primaryStage;
	// Array of cached scenes
	private final Map<String, Scene> sceneCache = new HashMap<>();
	// Current view index
	private String currentView = "";

	// Constructor that receives the Stage from App.java
	public SceneController(Stage stage) {
		// Set the primary stage
		primaryStage = stage;
		primaryStage.setMaximized(false);
		primaryStage.initStyle(StageStyle.TRANSPARENT);

		System.out.println("Initialized scene controller via App");
	}

	// Initialize the scene with the given dimensions
	public void initialize(double width, double height) throws Exception {
		primaryStage.setWidth(width);
		primaryStage.setHeight(height);

		primaryStage.setMinHeight(720);
		primaryStage.setMinWidth(1280);

		// Initialize scenes
		this.preloadScenes();

		// Go to the main app
		if (UserManager.isLoggedIn()) {
			this.switchToScene("Dashboard");
		} else {
			this.switchToScene("App");
			// this.switchToScene("Dashboard");
		}
	}

	// Preloading the scene and add it in the cache for fast scene loading
	private void preloadScenes() throws IOException {
		this.sceneCache.put("App", loadScene("App"));
		this.sceneCache.put("Login", loadScene("Login"));
		this.sceneCache.put("Register", loadScene("Register"));
		this.sceneCache.put("Device", loadScene("Device"));
		// this.sceneCache.put("Dashboard", loadScene("Dashboard"));
	}

	// Load the scenes and cache it
	private Scene loadScene(String sceneName) throws IOException {
		// Loader for FXML
		FXMLLoader root;
		// Scene for the node
		Scene scene;

		// Creating the scene object
		switch (sceneName) {
			case "App":
				root = new FXMLLoader(getClass().getResource("/views/desktop/app-view.fxml"));

				// Load the FXML and set the scene
				scene = new Scene(root.load(), Color.TRANSPARENT);

				// Access the AppController and set the SceneController
				AppController appController = root.getController();

				// Inject SceneController into AppController
				appController.setSceneController(this);

				scene.getProperties().put("controller", appController);
				break;
			case "Login":
				root = new FXMLLoader(getClass().getResource("/views/desktop/login-view.fxml"));

				// Load the FXML and set the scene
				scene = new Scene(root.load(), Color.TRANSPARENT);

				// Access the AppController and set the SceneController
				LoginController loginController = root.getController();

				// Inject SceneController into AppController
				loginController.setSceneController(this);

				scene.getProperties().put("controller", loginController);
				break;
			case "Register":
				root = new FXMLLoader(getClass().getResource("/views/desktop/register-view.fxml"));

				// Load the FXML and set the scene
				scene = new Scene(root.load(), Color.TRANSPARENT);

				// Access the AppController and set the SceneController
				RegisterController registerController = root.getController();

				// Inject SceneController into AppController
				registerController.setSceneController(this);

				scene.getProperties().put("controller", registerController);
				break;
			case "Dashboard":
				root = new FXMLLoader(getClass().getResource("/views/desktop/dashboard-view.fxml"));

				// Load the FXML and set the scene
				scene = new Scene(root.load(), Color.TRANSPARENT);

				// Access the AppController and set the SceneController
				DashboardController dashboardController = root.getController();

				// Inject SceneController into AppController
				dashboardController.setSceneController(this);

				scene.getProperties().put("controller", dashboardController);
				break;
			case "Device":
				root = new FXMLLoader(getClass().getResource("/views/desktop/device-view.fxml"));

				// Load the FXML and set the scene
				scene = new Scene(root.load(), Color.TRANSPARENT);

				// Access the AppController and set the SceneController
				DevicesController devicesController = root.getController();

				// Inject SceneController into AppController
				devicesController.setSceneController(this);

				scene.getProperties().put("controller", devicesController);
				break;

			default:
				throw new IllegalArgumentException("Scene not found: " + sceneName);
		}

		// Return scene
		return scene;
	}

	// Switch the stage scene
	public void switchToScene(String sceneName) {
		// Add it in the background to avoid it blocking JavaFX UI thread
		new Thread(() -> {
			try {
				// Check if scene is already cached, if not create it
				if (!sceneCache.containsKey(sceneName)) {
					this.sceneCache.put(sceneName, loadScene(sceneName));
				}
				// Current Scene
				Scene currentScene = this.sceneCache.get(sceneName);

				// Set current view index
				this.currentView = sceneName;
				System.out.println("current Scene : " + sceneName);

				// Get current sceneBaseController
				BaseSceneController sceneBaseController = (BaseSceneController) currentScene.getProperties()
						.get("controller");

				// Wait so UI transition can work smoothly
				Thread.sleep(150);

				Platform.runLater(() -> {
					// Set it into the stage
					primaryStage.setScene(currentScene);
					primaryStage.show();

					// Notify that view is opened
					if (sceneBaseController != null)
						sceneBaseController.onOpened();
				});

			} catch (Exception e) {
				System.out.println("Error occurred: " + e.getMessage());
			}
		}).start();
	}

	public void onClose() {
		// Current Scene
		Scene currentScene = this.sceneCache.get(currentView);

		if (currentScene != null) {
			// Get current sceneBaseController
			BaseSceneController sceneBaseController = (BaseSceneController) currentScene.getProperties()
					.get("controller");
			sceneBaseController.onClosed();
		}
	}
}