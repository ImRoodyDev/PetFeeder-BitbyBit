package com.bitbybit.controllers;

import com.bitbybit.base.BaseSceneController;
import com.bitbybit.components.NotificationText;
import com.bitbybit.services.authentication.UserManager;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterController extends BaseSceneController {
	private NotificationText notification;

	@FXML
	private TextField emailField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private JFXButton registerButton;
	@FXML
	private JFXButton loadingButton;
	@FXML
	private VBox registerForm;

	// Send a notification to the form
	private void sendFormNotification(String message) {
		Platform.runLater(() -> {
			try {
				if (notification == null) {
					notification = new NotificationText(4500);
					registerForm.getChildren().add(1, notification.getNodeComponent());
				}

				notification.setText(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void loadingState(boolean toggle) {
		//  Enable loading spinner
		this.loadingButton.setVisible(toggle);
		this.loadingButton.setManaged(toggle);

		this.registerButton.setVisible(!toggle);
		this.registerButton.setManaged(!toggle);
	}

	// This method will be automatically called after the FXML is loaded
	@Override
	public void initialize() {
		super.initialize();

	}

	@Override
	public void applyDynamicStyle() {
		super.applyDynamicStyle();

		if (loadingButton != null) {
			loadingButton.setVisible(false);
			loadingButton.setManaged(false);
		}
	}

	/**
	 * Set the sceneController
	 */
	public void setSceneController(SceneController controller) throws IOException {
		sceneController = controller;
	}

	/**
	 * Swithc to login scene
	 */
	public void switchToLogin() throws IOException {
		this.sceneController.switchToScene("Login");
	}

	public void register() throws IOException {
		// Get form inputs
		String email = emailField.getText();
		String password = passwordField.getText();

		// Check inputs
		if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
			sendFormNotification("Please fill an email and password");
			return;
		}

		// Set loading state on
		loadingState(true);

		// Thread to log in in the background
		new Thread(() -> {
			try {
				// Post request
				JSONObject response = UserManager.register(email, password);

				if (response.getInt("statusCode") == 200) {
					System.out.println(response.getString("message"));
					this.sceneController.switchToScene("Device");
				} else {
					sendFormNotification(response.getString("message"));
					loadingState(false);
				}

			} catch (Exception e) {
				sendFormNotification("An error occurred : ");
				loadingState(false);
			}
		}).start();
	}

}


