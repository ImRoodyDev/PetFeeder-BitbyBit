package com.bitbybit.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;

public class WeightComponent {
	public final String tijdstripname;
	public final Integer procent;
	private final Node element;

	@FXML
	private Label timestip;
	@FXML
	private Label procentLabel;
	@FXML
	private ProgressBar bar;

	public WeightComponent(String name, Integer p) throws IOException {
		// Initialize fields
		tijdstripname = name;
		procent = p;

		// Load FXML and set the controller
		FXMLLoader root = new FXMLLoader(getClass().getResource("/components/weight-component.fxml"));
		root.setController(this);
		element = root.load();

		// Initialize UI elements with provided values
		initializeUI();
	}

	// Method to return the root Node of the component
	public Node getNodeComponent() {
		return element;
	}

	// Initialize UI elements
	private void initializeUI() {
		// Set the timestamp label to the provided name
		if (timestip != null) {
			timestip.setText(tijdstripname);
		}

		// Set the percentage label and progress bar
		if (procentLabel != null) {
			procentLabel.setText(procent + "%");
		}

		if (bar != null) {
			// Set progress bar value (ensure it's between 0 and 1)
			bar.setProgress(Math.min(1.0, Math.max(0.0, procent / 100.0)));
		}
	}
}
