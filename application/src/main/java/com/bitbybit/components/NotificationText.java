package com.bitbybit.components;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.IOException;

public class NotificationText {
	@FXML
	private Label label;
	
	private final Node element;
	private Thread thread;
	private Integer TTL;

	public NotificationText(Integer aliveMS) throws IOException {
		FXMLLoader root = new FXMLLoader(getClass().getResource("/components/notification-component.fxml"));
		root.setController(this);
		element = root.load();
		TTL = aliveMS;
	}

	public void setText(String text) {
		if (thread != null && thread.isAlive()) {
			thread.interrupt(); // Stop the previous thread
		}

		if (label != null) {
			label.setText(text);
		}

		Platform.runLater(() -> {
			element.setVisible(true);
			element.setManaged(true);
		});

		// Start a new thread to disable the notification after 2 seconds
		thread = new Thread(() -> {
			try {
				Thread.sleep(TTL); // Wait for 2 seconds
				Platform.runLater(() -> {
					element.setVisible(false);
					element.setManaged(false);
				});
			} catch (InterruptedException e) {
				// Thread interrupted; just exit
			}
		});
		thread.setDaemon(true); // Ensure the thread won't block application exit
		thread.start();
	}

	public Node getNodeComponent() {

		//
		return element;
	}

}
