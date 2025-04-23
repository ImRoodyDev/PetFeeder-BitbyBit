package com.bitbybit.controllers;

import com.bitbybit.base.BaseSceneController;
import com.bitbybit.components.NotificationText;
import com.bitbybit.services.authentication.UserManager;
import com.bitbybit.utils.NetworkService;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class LoginController extends BaseSceneController {
	private NotificationText notification;
	private boolean loggingIn = false;

	@FXML
	private TextField emailField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private JFXButton loginButton;
	@FXML
	private VBox loginForm;

	// Send a notification to the form
	private void sendFormNotification(String message) {
		Platform.runLater(() -> {
			try {
				if (notification == null) {
					notification = new NotificationText(4500);
					loginForm.getChildren().add(1, notification.getNodeComponent());
				}

				notification.setText(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	// This method will be automatically called after the FXML is loaded
	@Override
	public void initialize() {
		super.initialize();
	}

	/** Set the sceneController */
	public void setSceneController(SceneController controller) throws IOException {
		sceneController = controller;
	}

	/** Switch to register scene */
	public void switchToRegister() throws IOException {
		this.sceneController.switchToScene("Register");
	}

	public void login() throws IOException {
		// Get form inputs
		String email = emailField.getText();
		String password = passwordField.getText();

		// Check inputs
		if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
			sendFormNotification("Please fill an email and password");
			return;
		}

		System.out.println("Email: " + email);
		System.out.println("Password: " + password);

		// Check if user is already logginIn
		if (loggingIn) {
			return;
		}

		//  Enable loading spinner
		this.loginButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);


		if (UserManager.isLoggedIn()) {
			sendFormNotification("User is already logged in please restart application");
			return;
		}

		// Thread to log in in the background
		new Thread(() -> {
			try {
				// Post request
				JSONObject response = UserManager.login(email, password);

				// Login button
				Platform.runLater(() -> loginButton.setDisable(true));

				if (response.getInt("statusCode") == 200) {
					System.out.println(response.getString("message"));

					if (response.getInt("ownedFeeders") <= 0) {
						this.sceneController.switchToScene("Device");
					} else {
						this.sceneController.switchToScene("Dashboard");
					}
				} else {
					// Login button
					Platform.runLater(() -> loginButton.setDisable(false));

					sendFormNotification(response.getString("message"));
				}
			} catch (Exception e) {
				e.printStackTrace();
				sendFormNotification("An error occurred");
			} finally {
				//  Disable loading spinner
				loggingIn = false;
				Platform.runLater(() -> loginButton.setContentDisplay(ContentDisplay.TEXT_ONLY));
				Platform.runLater(() -> loginButton.setDisable(false));
			}
		}).start();
	}

}
