package com.bitbybit.base;

import com.bitbybit.controllers.SceneController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class BaseSceneController {
	// Scene controller
	protected SceneController sceneController;
	// Always on false
	private boolean maximized = false;

	@FXML
	private AnchorPane appView;
	@FXML
	private Button closeWindowButton;
	@FXML
	private Button toggleMaximizeButton;
	@FXML
	private Button toggleMinimizeButton;
	@FXML
	private HBox dragArea;

	@FXML
	private StackPane backgroundContainer;
	@FXML
	private ImageView catImage;

	private double xOffset = 0;
	private double yOffset = 0;

	/**
	 * This method will be automatically called after the FXML is loaded
	 */
	protected void initialize() {
		initializeHeaderButtons();
		applyDynamicStyle();
	}

	/**
	 * Initialize dynamic styles
	 */
	protected void applyDynamicStyle() {
		// Bind the ImageView's width and height to the StackPane's width and height
		if (catImage != null) {
			catImage.fitHeightProperty().bind(backgroundContainer.heightProperty());
		}


		if (appView != null) {
			// Apply clipping to the parent AnchorPane
			Rectangle clip = new Rectangle();
			clip.widthProperty().bind(appView.widthProperty());
			clip.heightProperty().bind(appView.heightProperty());
			clip.setArcWidth(36); // Match the corner radius in your CSS
			clip.setArcHeight(36);
			appView.setClip(clip);
		}

	}

	protected final void initializeHeaderButtons() {
		if (closeWindowButton != null) {
			closeWindowButton.setOnAction(_ -> closeWindow()); // Attach close event
		}

		if (toggleMaximizeButton != null) {
			toggleMaximizeButton.setOnAction(_ -> toggleMaximization()); // Attach maximize toggle event
		}

		if (toggleMinimizeButton != null) {
			toggleMinimizeButton.setOnAction(_ -> minimizeWindow()); // Attach minimize event
		}

		if (dragArea != null) {
			enableWindowDragging(); // Enable dragging functionality
		}
	}

	protected final void closeWindow() {
		sceneController.primaryStage.close(); // Closes the application window
	}

	protected final void toggleMaximization() {
		maximized = !maximized;
		sceneController.primaryStage.setMaximized(maximized); // Toggles maximization state
	}

	protected final void minimizeWindow() {
		sceneController.primaryStage.setIconified(true); // Minimizes the window
	}

	private void enableWindowDragging() {
		// Record the initial position of the mouse on press
		dragArea.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});

		// Update the position of the window during drag
		dragArea.setOnMouseDragged(event -> {
			sceneController.primaryStage.setX(event.getScreenX() - xOffset);
			sceneController.primaryStage.setY(event.getScreenY() - yOffset);
		});
	}

	public void onOpened() {

	}

	public void onClosed() {

	}
}
