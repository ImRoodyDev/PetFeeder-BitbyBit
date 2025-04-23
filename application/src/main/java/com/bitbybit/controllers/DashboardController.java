package com.bitbybit.controllers;

import com.bitbybit.base.BaseSceneController;
import com.bitbybit.components.ScheduleComponent;
import com.bitbybit.components.WeightComponent;
import com.bitbybit.services.authentication.FeederConfiguration;
import com.bitbybit.services.authentication.FeederSocketEventListener;
import com.bitbybit.services.authentication.UserManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class DashboardController extends BaseSceneController {
	// Class-level scheduler instance
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	// Selected device
	private int selectedFeeder = 0;
	// Owned feeders
	private final ArrayList<FeederConfiguration> ownedFeeders = new ArrayList<>();

	// Current page
	private int currentPage = -1;
	// Current Page Node
	private Node currentPageNode;
	// Weight property
	private final StringProperty currentWeight = new SimpleStringProperty("## KG");
	// Next meal property
	private final StringProperty nextMeal = new SimpleStringProperty("## AM");
	// Eaten times property
	private final StringProperty eaten = new SimpleStringProperty("## X");
	private boolean websocketOnline = false;

	@FXML
	private HBox pageView;
	@FXML
	private VBox sideBar;
	@FXML
	private Label dashboardLabel;
	@FXML
	private Label weightLabel;
	@FXML
	private Label eatenLabel;
	@FXML
	private Label nextMealLabel;
	@FXML
	private JFXComboBox<String> feedersCombo;
	@FXML
	private JFXComboBox<String> portionCombo;
	@FXML
	private JFXComboBox<String> monthCombo;
	@FXML
	private VBox scheduleList;
	@FXML
	private JFXButton addTimestampsBt;
	@FXML
	private JFXButton saveBt;
	@FXML
	private BarChart<String, BigDecimal> barChart;
	@FXML
	private VBox procentList;
	@FXML
	private NumberAxis s;


	/**
	 * Function to switch between Devices-View pages
	 */
	private void showPage(int page) {

		new Thread(() -> {
			if (!websocketOnline) {
				System.out.println("Websocket init");
				initializeFeederSocket();
			}
		}).start();

		try {
			System.out.println("Showpage called");
			if (this.currentPage == page) return;

			// Loader for FXML
			FXMLLoader root;

			// Switch case for the pages in the device view
			switch (page) {
				case 0:
					root = new FXMLLoader(getClass().getResource("/pages/dashboard-home.fxml"));
					root.setController(this);
					this.currentPageNode = root.load();

					// Set default value to the current month
					String currentMonth = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

					// Update UI
					Platform.runLater(() -> {
						dashboardLabel.setText("Welcome! Explore your dashboard..");

						// Clear the page
						pageView.getChildren().clear();
						pageView.getChildren().add(this.currentPageNode);

						// Bind the properties to the labels
						if (weightLabel != null)
							weightLabel.textProperty().bind(currentWeight);
						if (eatenLabel != null)
							eatenLabel.textProperty().bind(eaten);
						if (nextMealLabel != null)
							nextMealLabel.textProperty().bind(nextMeal);

						// Initialize daily
						initializeBarChart(currentMonth);
						initializeDailyChart();

						// Update combo menu
						if (monthCombo != null) {
							monthCombo.getItems().addAll(
									java.util.Arrays.asList(
											"January", "February", "March", "April", "May", "June",
											"July", "August", "September", "October", "November", "December"
									)
							);

							// Add event listener for when the selection changes
							monthCombo.setOnAction(event -> {
								String selectedMonth = monthCombo.getValue(); // Get the selected month
								System.out.println("Selected month: " + selectedMonth);
								// Perform additional actions here
								initializeBarChart(selectedMonth);
							});

							monthCombo.setValue(currentMonth); // Set the default selected value
						}
					});
					break;
				case 1:
					root = new FXMLLoader(getClass().getResource("/pages/dashboard-schema.fxml"));
					root.setController(this);
					this.currentPageNode = root.load();

					// Update UI
					Platform.runLater(() -> {
						dashboardLabel.setText("Schedule feeding time");

						// Clear the page
						pageView.getChildren().clear();
						pageView.getChildren().add(this.currentPageNode);

						if (!this.ownedFeeders.isEmpty()) {
							// Reload selected feeder timestamps
							loadTimestamps(this.ownedFeeders.get(selectedFeeder).feederId);

							// Update the UI
							ArrayList<ScheduleComponent> schedules = this.ownedFeeders.get(selectedFeeder).feedTimestamps;

							// Add the timestamps component Node
							for (ScheduleComponent schedule : schedules) {
								// System.out.println("add timestamps UI");
								scheduleList.getChildren().add(schedule.getNodeComponent());
							}
						}

						// Setup buttons actions
						addTimestampsBt.setOnAction(_ -> this.createFeedTimestamp());

						// Save button action
						saveBt.setOnAction(_ -> this.onSaveConfigurations());
					});
					break;
				case 2: // Setup food portions
					root = new FXMLLoader(getClass().getResource("/pages/dashboard-kat.fxml"));
					root.setController(this);
					this.currentPageNode = root.load();

					// Update UI
					Platform.runLater(() -> {
						dashboardLabel.setText("Setup your kat profile");

						// Clear the page
						pageView.getChildren().clear();
						pageView.getChildren().add(this.currentPageNode);

						// Add all items from the ArrayList to the combo box
						if (feedersCombo != null) {
							for (FeederConfiguration feedConfig : ownedFeeders) {
								feedersCombo.getItems().add(feedConfig.feederId);
							}

							if (!ownedFeeders.isEmpty())
								feedersCombo.setValue(ownedFeeders.get(selectedFeeder).feederId);

							feedersCombo.valueProperty().addListener((_, _, newValue) -> {
								System.out.println("Selected feeder id: " + newValue);
								selectedFeeder = feedersCombo.getItems().indexOf(newValue);
							});
						}

						if (portionCombo != null) {
							if (!ownedFeeders.isEmpty()) {
								portionCombo.getItems().addAll(getPortionSizes());
								portionCombo.setValue(ownedFeeders.get(selectedFeeder).portionSize + " gram");
							}

							portionCombo.valueProperty().addListener((_, _, newValue) -> {
								System.out.println("Selected gram value: " + newValue);
								ownedFeeders.get(selectedFeeder).portionSize = Integer.parseInt(newValue.split(" ")[0]);
							});
						}

						// Setup buttons actions
						saveBt.setOnAction(_ -> this.onSaveConfigurations());
					});
					break;
				case 3:
					root = new FXMLLoader(getClass().getResource("/pages/dashboard-setting.fxml"));
					root.setController(this);
					this.currentPageNode = root.load();

					// Update UI
					Platform.runLater(() -> {
						dashboardLabel.setText("Setting");
						// Clear the page
						pageView.getChildren().clear();
						pageView.getChildren().add(this.currentPageNode);

						if (feedersCombo != null) {
							for (FeederConfiguration feedConfig : ownedFeeders) {
								feedersCombo.getItems().add(feedConfig.feederId);
							}

							if (!ownedFeeders.isEmpty())
								feedersCombo.setValue(ownedFeeders.get(selectedFeeder).feederId);

							feedersCombo.valueProperty().addListener((_, _, newValue) -> {
								System.out.println("Selected feeder id: " + newValue);
								selectedFeeder = feedersCombo.getItems().indexOf(newValue);
							});
						}
					});
					break;
				default:
					break;
			}

			// Update current page name just in case its updated
			this.currentPage = page;
		} catch (Exception e) {
			System.out.println("Fail to open page in dashboard view");
			throw new RuntimeException(e);
		}
	}

	/** Menu var switch */
	public void switchDashboard(int page) {
		if (this.currentPage == page) return;

		// Process switch in the background for smooth ui animation
		new Thread(() -> {
			// UI update
			Platform.runLater(() -> {
				// Remove selected from the current class
				for (Node jfxButton : sideBar.getChildren()) {
					jfxButton.getStyleClass().remove("selected");
				}

				// Set selected for the current side button
				JFXButton currentSideButton = (JFXButton) sideBar.getChildren().get(page);
				if (!currentSideButton.getStyleClass().contains("selected")) {
					currentSideButton.getStyleClass().add("selected");
				}
			});

			// Switch to new page
			showPage(page);

			//
			System.out.println("Current page is " + currentPage);
		}).start();
	}

	/**
	 * Set the sceneController
	 */
	public void setSceneController(SceneController controller) {
		sceneController = controller;
	}

	/** Available Portion sizes */
	private ArrayList<String> getPortionSizes() {
		ArrayList<String> portionSizes = new ArrayList<>();
		// Add gram values (100g to 900g)
		for (int i = 25; i <= 5000; i += 25) {
			portionSizes.add(i + " gram");
		}

		return portionSizes;
	}

	/** Initialize Feeder socket connection */
	private void initializeFeederSocket() {
		// Setup event listener
		FeederSocketEventListener feederSocketEventListener = new FeederSocketEventListener() {
			@Override
			public void onMessageReceived(String message) {
				// System.out.println(message);

				if (message.startsWith("WEIGHT:")) {
					currentWeight.set(message.split(":")[1] + "g");
				} else if (message.startsWith("NEXTFEED")) {
					String TEXT = message.split("-")[1] != null ? message.split("-")[1] : "N/A";
					nextMeal.setValue(TEXT);
				} else if (message.startsWith("ATE:")) {
					eaten.setValue(message.split(":")[1] + " X");
				} else if (message.startsWith("UPDATEWEIGHT")) {
					initializeDailyChart();
				}

			}

			@Override
			public void onClose() {
				websocketOnline = false;
			}
		};

		// Set eventListener
		UserManager.setFeederWebEventListener(feederSocketEventListener);

		// Initialize webSocketConnection
		if (!ownedFeeders.isEmpty()) {
			boolean connected = UserManager.connectWebSocket(ownedFeeders.get(selectedFeeder).feederId);
			websocketOnline = connected;
			if (connected) {
				System.out.println("Websocket Connected successfully");
			} else {
				System.out.println("Not connected to web socket");
			}
		} else {
			System.out.println("No linked pet feeder to the account");
		}
	}

	/** Initialize user owned/linked pet feeders */
	private void initializeOwnedFeeders() {
		try {
			// Get user owned feeders
			JSONObject response = UserManager.ownedFeeders();

			// Check if "data" key exists and is a JSON array
			if (response.has("data") && response.getInt("statusCode") == 200) {
				JSONArray data = response.getJSONArray("data");

				// Loop through the JSON array and add elements to the ArrayList
				for (int feederIndex = 0; feederIndex < data.length(); feederIndex++) {
					// Retrieve each JSON object
					JSONObject feederData = data.getJSONObject(feederIndex);
					JSONArray dataTimestamps = feederData.getJSONArray("timestamps");

					// Create feeder configuration profile
					FeederConfiguration feederConfig = new FeederConfiguration(feederData.getString("feederId"), feederData.getInt("portionSize"));

					// Timestamps array
					ArrayList<ScheduleComponent> timestampsComponents = new ArrayList<>();

					// Loop through all the timestamps
					for (int j = 0; j < dataTimestamps.length(); j++) {
						JSONObject timestamp = dataTimestamps.getJSONObject(j);
						ScheduleComponent component = new ScheduleComponent(timestamp.getString("id"), j, timestamp.getString("time"));

						component.onDelete(() -> deleteTimestamp(component.id, component.index));
						timestampsComponents.add(component);
					}

					// Add the array timestamps to the feeder profiles
					feederConfig.addTimestampsComponent(timestampsComponents);

					// Add feeder to owned feeders list
					ownedFeeders.add(feederConfig);
				}

			} else {
				System.out.println(response.getString("message"));
			}
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Failed to initialize owned feeders");
		}
	}

	/** Set bar chart up */
	private void initializeBarChart(String month) {
		try {
			if (ownedFeeders.isEmpty()) return;
			String deviceId = this.ownedFeeders.get(selectedFeeder).feederId;

			JSONObject response = UserManager.getWeeklyWeights(deviceId, month, LocalDate.now().getYear());
			JSONArray weights = response.getJSONArray("data");

			barChart.setCategoryGap(80);
			barChart.setBarGap(80);

			XYChart.Series series = new XYChart.Series();
			series.setName(month);

			// Loop through all the weights
			for (int j = weights.length() - 1; j > 0; j--) {
				String week = weights.getJSONObject(j).getString("week");
				BigDecimal percentage = weights.getJSONObject(j).getBigDecimal("average");
				series.getData().add(new XYChart.Data(week, percentage));
			}

			barChart.getData().clear();
			barChart.getData().add(series);
		} catch (Exception e) {
			System.out.println("Failed to init chart: " + e.getMessage());
		}
	}

	private void initializeDailyChart() {
		try {
			if (ownedFeeders.isEmpty()) return;

			// Get the device ID, current month, day, and year
			String deviceId = this.ownedFeeders.get(selectedFeeder).feederId;
			String currentMonth = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
			Integer day = LocalDate.now().getDayOfMonth();

			// Fetch daily weights from the UserManager
			JSONObject response = UserManager.getDailyWeights(deviceId, day, currentMonth, LocalDate.now().getYear());
			JSONArray weights = response.getJSONArray("data");

			// Clear existing components in the procentList if weights are available
			if (!weights.isEmpty()) {
				procentList.getChildren().clear();
			}

			// Loop through all the weights
			for (int j = 0; j < weights.length(); j++) {
				// Extract data for each weight
				String name = "Tijdstip " + (j + 1); // Dynamic label for timestamp
				Float percentage = weights.getJSONObject(j).getFloat("average");

				// Create a WeightComponent instance
				WeightComponent component = new WeightComponent(name, Math.round(percentage));

				// Add the component's Node to the procentList (VBox, HBox, etc.)
				procentList.getChildren().add(component.getNodeComponent());
			}
		} catch (Exception e) {
			System.out.println("Failed to init chart: " + e.getMessage());
			e.printStackTrace();
		}
	}


	private void loadTimestamps(String deviceId) {
		try {
			System.out.println("loadTimestamps");
			// Load for the selected feeder the timestamps
			scheduleList.getChildren().clear();

			JSONObject response = UserManager.feederTimestamps(deviceId);
			JSONArray dataTimestamps = response.getJSONArray("data");

			// Loop through all the timestamps
			for (int j = 0; j < dataTimestamps.length(); j++) {
				JSONObject timestamp = dataTimestamps.getJSONObject(j);
				ScheduleComponent component = new ScheduleComponent(timestamp.getString("id"), j, timestamp.getString("time"));
				component.onDelete(() -> deleteTimestamp(component.id, component.index));
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Create new timestamps for the selected feeder */
	private void createFeedTimestamp() {
		try {
			if (ownedFeeders.isEmpty()) return;
			FeederConfiguration config = ownedFeeders.get(selectedFeeder);
			JSONObject response = UserManager.createTimestamp(config.feederId);

			if (response.getInt("statusCode") == 200) {
				// Add timestamps
				config.addTimestamp(response.getString("id"));

				// Position of the timestamps index
				int position = Math.max((config.feedTimestamps.size() - 1), 0);
				System.out.println("Create position : " + position);

				// Get teh component
				ScheduleComponent component = config.feedTimestamps.get(position);
				component.onDelete(() -> deleteTimestamp(component.id, component.index));

				Platform.runLater(() -> {
					scheduleList.getChildren().add(component.getNodeComponent());
				});
			} else {
				System.out.println(response.getString("message"));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Delete timestamps for the selected feeder */
	private void deleteTimestamp(String id, int index) {
		try {
			JSONObject response = UserManager.deleteTimestamp(id);

			if (response.getInt("statusCode") == 200) {
				ownedFeeders.get(selectedFeeder).removeTimestamp(index);
				scheduleList.getChildren().remove(index);
			} else {
				System.out.println(response.getString("message"));
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Event fired on save button clicked */
	public void onSaveConfigurations() {
		FeederConfiguration feeder = ownedFeeders.get(selectedFeeder);

		if (this.currentPage == 1) {
			try {
				ArrayList<ScheduleComponent> config = feeder.feedTimestamps;
				for (ScheduleComponent component : config) {
					if (component.updated) {
						JSONObject response = UserManager.updateTimestamp(ownedFeeders.get(selectedFeeder).feederId, component.id, component.getTimeStamp());

						if (response.getInt("statusCode") == 200) {
							component.updated = false;
							System.out.println("Updated timestamps");
						} else {
							System.out.println(response.getString("message"));
						}
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (this.currentPage == 2) {
			try {
				JSONObject response = UserManager.updatePortion(feeder.feederId, feeder.portionSize);
				if (response.getInt("statusCode") == 200) {
					System.out.println("Update portion size");
				} else {
					System.out.println(response.getString("message"));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void logout() throws Exception {
		UserManager.logout();
		this.sceneController.switchToScene("App");
	}

	public void linkNewFeeder() {
		this.sceneController.switchToScene("Device");
	}

	public void deleteFeeder() {

	}

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void onOpened() {
		// ...
		System.out.println("Dashboard view is opened");

		// List of sidebarButtons
		List<Node> sidebarButtons = sideBar.getChildren();

		// Initialize sidebar buttons
		for (int i = 0; i < sidebarButtons.size(); i++) {
			JFXButton jfxButton = (JFXButton) sidebarButtons.get(i);
			int finalI = i;
			jfxButton.setOnAction(_ -> switchDashboard(finalI));
		}

		initializeOwnedFeeders();

		// Show default page
		this.showPage(0);
	}

	@Override
	public void onClosed() {
		scheduler.shutdownNow();
		System.out.println("Dashboard view is closed");
	}
}
