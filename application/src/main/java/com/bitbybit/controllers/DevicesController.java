package com.bitbybit.controllers;

import com.bitbybit.animations.ScaleAnimation;
import com.bitbybit.animations.SpinnersAnimation;
import com.bitbybit.base.BaseSceneController;
import com.bitbybit.components.DeviceComponent;
import com.bitbybit.services.authentication.UserManager;
import com.bitbybit.services.bluetooth.BLEDevice;
import com.bitbybit.services.bluetooth.BluetoothBLE;
import com.bitbybit.services.bluetooth.BluetoothEventListener;
import com.jfoenix.controls.JFXButton;

import com.jfoenix.controls.JFXRadioButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DevicesController extends BaseSceneController {
	// Class-level scheduler instance
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	// Bluetooth service to handle device discovery and connections
	private final BluetoothBLE bluetoothService = new BluetoothBLE();

	// Current page
	private String currentPage = "";
	// Switch page wait time
	private int switchPageMs = 2500;
	// Current Page Node
	private Node currentPageNode;

	// Selected device
	private String selectedDevice;
	// Device wifi state
	private String deviceWifiState = "";
	// Device is linked
	private boolean deviceLinkedState = false;
	// Device is linked failed
	private boolean deviceLinkFailed = false;
	// Device serial
	private String deviceSerialNumber = "";
	// Device init state
	private String deviceUartState = "";
	// A rechecking count for the device
	private int deviceRecheckingCount = 5;
	// Device wait to connect in seconds
	private int deviceWaitTime = 25;
	// List of all the devices
	private final List<DeviceComponent> discoveredDevices = new ArrayList<>();
	// The ToggleGroup for the RadioButtons
	private ToggleGroup deviceGroup = new ToggleGroup();

	@FXML
	private AnchorPane pageView;
	@FXML
	private VBox deviceList;
	@FXML
	private JFXButton searchButton;
	@FXML
	private SVGPath searchBtSpinner;
	@FXML
	private JFXButton connectButton;
	@FXML
	private JFXButton backButton;
	@FXML
	private Circle loadingCircle;
	@FXML
	private Label loadingLabel;
	@FXML
	private MFXProgressSpinner spinner;
	@FXML
	private TextField wifiField;
	@FXML
	private PasswordField wifiPasswordField;

	/**
	 * Function to switch between Devices-View pages
	 */
	private void showPage(String pageName) {
		try {
			if (this.currentPage.equals(pageName)) return;

			// Loader for FXML
			FXMLLoader root;

			// Switch case for the pages in the device view
			switch (pageName) {
				case "connect-device":
					root = new FXMLLoader(getClass().getResource("/pages/connect-device.fxml"));
					root.setController(this);
					this.currentPageNode = root.load();

					// Update UI
					Platform.runLater(() -> {
						// Clear the page
						pageView.getChildren().clear();
						pageView.getChildren().add(this.currentPageNode);
						backButton.setVisible(false); // Hides the button visually
						backButton.setManaged(false); // Removes it from the layout
					});
					break;
				case "loading":
					root = new FXMLLoader(getClass().getResource("/pages/loading-device.fxml"));
					root.setController(this);
					this.currentPageNode = root.load();

					// Update UI
					Platform.runLater(() -> {
						// Clear the page
						pageView.getChildren().clear();
						pageView.getChildren().add(this.currentPageNode);
						backButton.setVisible(false); // Hides the button visually
						backButton.setManaged(false); // Removes it from the layout

						// Start animations
						ScaleAnimation.startScaler(this.loadingCircle, -0.2, 0.8);
					});
					break;
				case "setup-device":
					root = new FXMLLoader(getClass().getResource("/pages/setup-device.fxml"));
					root.setController(this);
					this.currentPageNode = root.load();

					// Update UI
					Platform.runLater(() -> {
						// Clear the page
						pageView.getChildren().clear();
						pageView.getChildren().add(this.currentPageNode);

						backButton.setVisible(false); // Hides the button visually
						backButton.setManaged(false); // Removes it from the layout
					});
					break;
				default:
					break;
			}

			// Update current page name just in case its updated
			this.currentPage = pageName;
		} catch (Exception e) {
			System.out.println("Fail to open page in device view");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Initialize Bluetooth service
	 */
	public void initializeBluetooth() {
		String serviceId = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
		String txId = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
		String rxId = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

		// Initialize bluetooth services
		bluetoothService.initializeServices(serviceId, txId, rxId);

		// Initialize BluetoothEventListener
		BluetoothEventListener listener = new BluetoothEventListener() {
			@Override
			public void onDevicesDiscovered(List<BLEDevice> devices) {
				System.out.println("Discover completed");

				try {
					if (!currentPage.equals("connect-device")) {
						return;
					}

					// Device nodes
					List<Node> deviceNodes = new ArrayList<>();
					// Loop through all the discovered devices
					for (BLEDevice device : devices) {
						System.out.println("Found: " + device.getName() + " : " + device.getId());
						DeviceComponent deviceComp = new DeviceComponent(device.getName(), device.getId(), deviceGroup);
						discoveredDevices.add(deviceComp);
						deviceNodes.add(deviceComp.getNodeComponent());
					}

					// Update the UI in a single runLater call
					Platform.runLater(() -> {
						// UI update logic
						connectButton.setDisable(false);
						searchButton.setDisable(false);
						searchButton.setText("Search again for devices");
						SpinnersAnimation.stopSpinner();
						deviceList.getChildren().addAll(deviceNodes);
					});

				} catch (IOException e) {
					System.out.print(e.getMessage());
				}

			}

			@Override
			public void onDiscoveryFailed() {
				System.out.println("Discover failed");

				// Update the UI in a single runLater call
				Platform.runLater(() -> {
					// UI update logic
					connectButton.setDisable(false);
					searchButton.setDisable(false);
					searchButton.setText("Search again for devices");
					SpinnersAnimation.stopSpinner();
				});
			}

			@Override
			public void onDeviceFailConnect() {
				if (currentPage.equals("loading")) {
					ScaleAnimation.stopScaler();
					spinner.visibleProperty().set(false);
					loadingCircle.setFill(Color.web("#e74c3c"));
					Platform.runLater(() -> loadingLabel.setText("Fail to connect to: " + selectedDevice));
					scheduler.schedule(() -> Platform.runLater(() -> showPage("connect-device")), 1500, TimeUnit.MILLISECONDS);
				}
			}

			@Override
			public void onDeviceConnected(String deviceId) {
				System.out.println("Connected feeder id : " + deviceId);

				// If it's on the loading page
				if (currentPage.equals("loading")) {
					loadingCircle.setFill(Color.web("#f39c12"));
					Platform.runLater(() -> loadingLabel.setText("Connected to feeder successfully, starting feeder...."));

					// Wait the embedded device init state to be updated and force it to be updated
					int count = 0;
					while (count < deviceRecheckingCount && !deviceUartState.equals("1")) {
						try {
							count++;
							bluetoothService.sendSerialMessage("UART:GET");
							if (count == deviceRecheckingCount) {
								Platform.runLater(() -> loadingLabel.setText("Failed to start the feeder, please replug in the power."));
							}
							Thread.sleep(1500);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					// Check what is the init state
					if (deviceUartState.equals("1")) {
						ScaleAnimation.stopScaler();
						spinner.visibleProperty().set(false);
						loadingCircle.setFill(Color.web("#27ae60"));

						Platform.runLater(() -> loadingLabel.setText("Feeder started successfully...."));
						scheduler.schedule(() -> showPage("setup-device"), switchPageMs, TimeUnit.MILLISECONDS);
					} else {
						ScaleAnimation.stopScaler();
						spinner.visibleProperty().set(false);
						loadingCircle.setFill(Color.web("#e74c3c"));

						// Disconnect device
						bluetoothService.disconnectDevice();
						scheduler.schedule(() -> showPage("connect-device"), switchPageMs, TimeUnit.MILLISECONDS);
					}
				}
			}

			@Override
			public void onDeviceDisconnected() {
				if (currentPage.equals("loading")) {
					spinner.visibleProperty().set(false);
					ScaleAnimation.stopScaler();
					loadingCircle.setFill(Color.web("#e74c3c"));
					Platform.runLater(() -> loadingLabel.setText("Device disconnected"));
					// Go back to discover
					scheduler.schedule(() -> Platform.runLater(() -> showPage("connect-device")), 1500, TimeUnit.MILLISECONDS);
				}
			}

			@Override
			public void onSerialReceived(String message) {
				if (message.contains("WIFI:")) {
					try {
						deviceWifiState = message.split(":")[1].trim();

						// Link device
						JSONObject response = UserManager.linkFeeder(bluetoothService.getConnectedDevice());

						// Update UI
						Platform.runLater(() -> {
							if (response.getInt("statusCode") == 200) {
								deviceLinkedState = true;
								deviceLinkFailed = false;

								if (currentPage.equals("loading")) {
									if (deviceWifiState.equals("1")) {
										ScaleAnimation.stopScaler();
										spinner.visibleProperty().set(false);
										loadingCircle.setFill(Color.web("#27ae60"));
										loadingLabel.setText("Feeder is connected to WiFi successfully");
										scheduler.schedule(() -> switchToHome(), switchPageMs, TimeUnit.MILLISECONDS);
									} else if (deviceWifiState.equals("0")) {
										ScaleAnimation.stopScaler();
										spinner.visibleProperty().set(false);
										loadingCircle.setFill(Color.web("#e74c3c"));
										loadingLabel.setText("Feeder failed to connect to WiFi successfully");
										scheduler.schedule(() -> showPage("setup-device"), switchPageMs, TimeUnit.MILLISECONDS);
									}
								}
							} else {
								deviceLinkedState = false;
								deviceLinkFailed = true;

								if (currentPage.equals("loading")) {
									ScaleAnimation.stopScaler();
									spinner.visibleProperty().set(false);
									loadingCircle.setFill(Color.web("#e74c3c"));
									loadingLabel.setText(response.getString("message"));
									scheduler.schedule(() -> showPage("setup-device"), switchPageMs, TimeUnit.MILLISECONDS);
								}
							}
						});
					} catch (Exception e) {
						deviceLinkedState = false;
						deviceLinkFailed = true;

						// Update UI
						Platform.runLater(() -> {
							if (currentPage.equals("loading")) {
								ScaleAnimation.stopScaler();
								spinner.visibleProperty().set(false);
								loadingCircle.setFill(Color.web("#e74c3c"));
								loadingLabel.setText(e.getMessage());
								// Go to setup device Wi-Fi credentials
								scheduler.schedule(() -> showPage("setup-device"), switchPageMs, TimeUnit.MILLISECONDS);
							}
						});
					}
				} else if (message.contains("UART:")) {
					deviceUartState = message.split(":")[1].trim();
					System.out.println("Init state received : " + deviceUartState);

				} else if (message.contains("SR:")) {
					deviceSerialNumber = message.split(":")[1].trim();
					System.out.println("Serial received : " + deviceSerialNumber);

				}
			}
		};

		// Set up initializing
		bluetoothService.setEventListener(listener);
	}

	/**
	 * Set the sceneController
	 */
	public void setSceneController(SceneController controller) {
		sceneController = controller;
	}

	// Search for bluetooth devices
	public void searchBluetoothDevices() {
		if (!this.discoveredDevices.isEmpty()) {
			this.discoveredDevices.clear();
		}

		// Disable connect button
		this.connectButton.setDisable(true);
		this.searchButton.setDisable(true);
		this.searchButton.setText("Searching....");
		SpinnersAnimation.startSpinner(this.searchBtSpinner);

		// Clear Menu list children
		this.deviceList.getChildren().clear();

		// Call for bluetooth discover
		bluetoothService.discoverBleDevices();
	}

	// Connected to bluetooth device
	public void connectDevice() {
		// Check for toggle
		Toggle toggle = deviceGroup.getSelectedToggle();
		if (toggle == null) {
			return;
		}

		selectedDevice = ((JFXRadioButton) toggle).getText();
		if (selectedDevice != null) {
			System.out.println("Selected device id :" + selectedDevice);

			// Switch to connecting device page
			this.showPage("loading");
			this.loadingLabel.setText("Please wait connecting to the feeder....");

			// Connect device
			bluetoothService.connectToDevice(selectedDevice);
		} else {
			System.out.println("No device is selected");
		}
	}

	/**
	 * Switch to register scene
	 */
	public void switchToHome() {
		this.sceneController.switchToScene("Dashboard");
	}

	/** Send Wi-Fi credentials */
	public void sendWiFiCredentials() {
		if (wifiPasswordField == null && wifiField == null) {
			return;
		}

		// Wi-Fi and Password field
		String wifi = wifiField.getText();
		String password = wifiPasswordField.getText();

		// Show loading page
		showPage("loading");
		this.loadingLabel.setText("Please wait sending credentials to the feeder....");

		// reset variables
		deviceLinkedState = false;
		deviceLinkFailed = false;

		// Send data in sequence
		new Thread(() -> {
			try {
				Thread.sleep(1500);
				bluetoothService.sendSerialMessage("SD:" + wifi);
				Thread.sleep(1500);
				bluetoothService.sendSerialMessage("PS:" + password);
				Thread.sleep(3500);
				bluetoothService.sendSerialMessage("WIFI:CONNECT");

				// Check after 15 second if the device is not connected to Wi-Fi
				scheduler.schedule(() -> {
					int count = 0;
					while (count < deviceRecheckingCount && !deviceWifiState.equals("1") || count < deviceRecheckingCount && !deviceLinkedState) {
						try {
							count++;
							// Send Command to get WIFI Status
							bluetoothService.sendSerialMessage("WIFI:GET");

							if (count == deviceRecheckingCount && deviceWifiState == null || count == deviceRecheckingCount && !deviceLinkedState) {
								Platform.runLater(() -> {
									loadingLabel.setText("Please replug the feeder in power an error has occurred");
									scheduler.schedule(() -> showPage("setup-device"), switchPageMs, TimeUnit.MILLISECONDS);
								});

							} else if (count == deviceRecheckingCount && !deviceWifiState.equals("1")) {
								Platform.runLater(() -> loadingLabel.setText("Feeder failed to connect to WiFi"));
							} else if (deviceLinkFailed) {
								return;
							}

							// Wait 2 sec for next loop
							Thread.sleep(2000);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}, 10, TimeUnit.SECONDS);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).start();
	}

	public void goBackwards() {
		showPage("connect-device");
	}

	@Override
	public void initialize() {
		super.initialize();

		// Initialize the ToggleGroup
		deviceGroup = new ToggleGroup();

		// Initialize service
		initializeBluetooth();
	}

	@Override
	public void onOpened() {
		// ...
		System.out.println("Device view is opened");

		// Show default page
		this.showPage("connect-device");
	}

	@Override
	public void onClosed() {
		scheduler.shutdownNow();
		bluetoothService.clear();
		System.out.println("Device view is closed");
	}
}
