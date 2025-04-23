package com.bitbybit.components;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ScheduleComponent {
	public String id;
	public int index;
	public String hours;
	public String minutes;
	public boolean updated = false;
	private Runnable onChangeEvent;

	private final Node element;
	@FXML
	private Label timeLabel;
	@FXML
	private JFXComboBox<String> hoursCombo;
	@FXML
	private JFXComboBox<String> minutesCombo;
	@FXML
	private JFXButton deleteButton;


	public ScheduleComponent(String scheduleId, int position, String timestamp) throws IOException {
		id = scheduleId;
		index = position;
		hours = timestamp.split(":")[0] + " HH";
		minutes = timestamp.split(":")[1] + " MM";

		FXMLLoader root = new FXMLLoader(getClass().getResource("/components/schedule-time-component.fxml"));
		root.setController(this);
		element = root.load();

		timeLabel.setText("Tijdstip " + (index + 1));

		if (hoursCombo != null) {
			hoursCombo.getItems().addAll(getHoursValues());

			if (!timestamp.split(":")[0].equals("null"))
				hoursCombo.setValue(hours);

			hoursCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
				hours = newValue;
				if (!newValue.equals(oldValue)) {
					updated = true;
					if (onChangeEvent != null) {
						onChangeEvent.run();
					}
				}
			});
		}
		if (minutesCombo != null) {
			minutesCombo.getItems().addAll(getMinutesValues());

			if (!timestamp.split(":")[1].equals("null"))
				minutesCombo.setValue(minutes);

			minutesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
				minutes = newValue;

				if (!newValue.equals(oldValue)) {
					updated = true;
					if (onChangeEvent != null) {
						onChangeEvent.run();
					}
				}
			});
		}
	}

	public Node getNodeComponent() {
		return element;
	}

	private ArrayList<String> getHoursValues() {
		ArrayList<String> values = new ArrayList<>();
		// Add values in HH format
		for (int i = 0; i <= 24; i++) {
			// Format integers to "02 HH", "03 HH", etc.
			String formattedValue = String.format("%02d HH", i);
			values.add(formattedValue);
		}

		return values;
	}

	private ArrayList<String> getMinutesValues() {
		ArrayList<String> values = new ArrayList<>();
		// Add values in HH format
		for (int i = 0; i <= 60; i++) {
			// Format integers to "02 HH", "03 HH", etc.
			String formattedValue = String.format("%02d MM", i);
			values.add(formattedValue);
		}

		return values;
	}

	public void onValueChange(Runnable callable) {
		this.onChangeEvent = callable;
	}

	public void setPosition(int position) {
		index = position;
		timeLabel.setText("Tijdstip " + (index + 1));
	}

	public String getTimeStamp() {
		return hours.split(" ")[0] + ":" + minutes.split(" ")[0];
	}

	public void onDelete(Runnable event) {
		if (event != null) {
			deleteButton.setOnAction(e -> {
				event.run();
			});
		}
	}

}
