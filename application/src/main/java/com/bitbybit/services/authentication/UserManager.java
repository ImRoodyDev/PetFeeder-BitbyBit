package com.bitbybit.services.authentication;

import com.bitbybit.utils.NetworkService;

import java.io.IOException;
import java.util.Map;

import javafx.application.Platform;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class UserManager {
	private static FeederSocketEventListener feederWebEventListener;

	private static final String BASE_URL = "http://localhost:3002/api"; // Change this to your Node.js server URL

	public static WebSocket webSocket;

	/** Login method - returns a JSONObject containing status code and message */
	public static JSONObject login(String email, String password) {
		try {
			JSONObject response = NetworkService.post(
					BASE_URL + "/login",
					Map.of("email", email, "password", password),
					null, // No access token
					null  // No refresh token
			);

			if (response.getInt("statusCode") == 200) {
				// Extract tokens from response headers (not required here, for example, handle separately)
				String accessToken = response.getString("accessToken");
				String refreshToken = response.getString("refreshToken");

				if (accessToken != null && refreshToken != null) {
					TokenManager.saveTokens(accessToken, refreshToken);
				}
			}
			return response;
		} catch (Exception error) {
			System.out.println("Failed to send request to server: " + error.getMessage());
			return null;
		}
	}

	/** Register method - returns a JSONObject containing status code and message */
	public static JSONObject register(String email, String password) throws Exception {
		try {
			JSONObject response = NetworkService.post(
					BASE_URL + "/register",
					Map.of("email", email, "password", password),
					null, // No access token
					null  // No refresh token
			);

			if (response.getInt("statusCode") == 200) {
				// Extract tokens from response headers (not required here, for example, handle separately)
				String accessToken = response.getString("accessToken");
				String refreshToken = response.getString("refreshToken");

				if (accessToken != null && refreshToken != null) {
					TokenManager.saveTokens(accessToken, refreshToken);
				}
			}
			return response;
		} catch (Exception error) {
			System.out.println("Failed to send request to server: " + error.getMessage());
			return null;
		}
	}

	/** Link pet feeder to user */
	public static JSONObject linkFeeder(String deviceId) throws Exception {
		System.out.println("Link feeder");
		String[] tokens = TokenManager.loadTokens();

		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		JSONObject response = NetworkService.post(
				BASE_URL + "/link-device",
				Map.of("id", deviceId),
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);

		return response;
	}

	/** Setup portionSize */
	public static JSONObject updatePortion(String deviceId, int size) throws Exception {
		String[] tokens = TokenManager.loadTokens();
		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		// HTTPS Request to the server
		JSONObject response = NetworkService.post(
				BASE_URL + "/feeder/update-portion/" + deviceId,
				Map.of("weight", String.valueOf(size)),
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);

		return response;
	}

	/** Create feeder timestamps */
	public static JSONObject feederTimestamps(String id) throws Exception {
		String[] tokens = TokenManager.loadTokens();

		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		// HTTPS Request to the server
		JSONObject response = NetworkService.get(
				BASE_URL + "/feeder/timestamps/" + id,
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);

		return response;
	}

	/** Get weights */
	public static JSONObject getWeeklyWeights(String id, String month, Integer Year) throws Exception {
		String[] tokens = TokenManager.loadTokens();

		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		// HTTPS Request to the server
		JSONObject response = NetworkService.get(
				BASE_URL + "/feeder/weights/weekly/" + id + "?month=" + month + "&year=" + Year,
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);

		return response;
	}

	/** Get weights */
	public static JSONObject getDailyWeights(String id, Integer day, String month, Integer Year) throws Exception {
		String[] tokens = TokenManager.loadTokens();

		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		// HTTPS Request to the server
		JSONObject response = NetworkService.get(
				BASE_URL + "/feeder/weights/daily/" + id + "?month=" + month + "&year=" + Year + "&day=" + day,
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);

		return response;
	}

	/** Create new timestamps */
	public static JSONObject createTimestamp(String id) throws Exception {
		String[] tokens = TokenManager.loadTokens();

		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		// HTTPS Request to the server
		JSONObject response = NetworkService.get(
				BASE_URL + "/feeder/create-timestamp/" + id,
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);

		return response;
	}

	/** Update new timestamps */
	public static JSONObject updateTimestamp(String deviceId, String id, String time) throws Exception {
		String[] tokens = TokenManager.loadTokens();

		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		// HTTPS Request to the server
		JSONObject response = NetworkService.post(
				BASE_URL + "/feeder/update-timestamp/" + deviceId,
				Map.of("timestampId", id, "time", time),
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);

		return response;
	}

	/** Create new timestamps */
	public static JSONObject deleteTimestamp(String id) throws Exception {
		String[] tokens = TokenManager.loadTokens();

		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		// HTTPS Request to the server
		JSONObject response = NetworkService.get(
				BASE_URL + "/feeder/delete-timestamp/" + id,
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);

		return response;
	}

	/** Owned pet feeder of the user */
	public static JSONObject ownedFeeders() throws Exception {
		String[] tokens = TokenManager.loadTokens();

		if (tokens == null) {
			throw new IllegalStateException("User not logged in.");
		}

		String accessToken = tokens[0];
		String refreshToken = tokens[1];

		// HTTPS Request to the server
		JSONObject response = NetworkService.get(
				BASE_URL + "/feeders",
				accessToken, // Include access token in header
				refreshToken // Include refresh token as a cookie
		);


		return response;
	}

	/** Refresh token */
	public static boolean verifyTokens() {
		try {
			String[] tokens = TokenManager.loadTokens();
			if (tokens == null) {
				throw new IllegalStateException("User not logged in.");
			}

			String accessToken = tokens[0];
			String refreshToken = tokens[1];

			JSONObject response = NetworkService.get(
					BASE_URL + "/auth/verify",
					accessToken, // Include access token in header
					refreshToken // Include refresh token as a cookie
			);

			return response.getInt("statusCode") == 200;
		} catch (Exception error) {
			System.out.println("Failed to send request to server: " + error.getMessage());
			return false;
		}
	}

	/** Connect Web socket */
	public static boolean connectWebSocket(String deviceId) {
		try {
			if (webSocket != null) {
				return true;
			}

			String[] tokens = TokenManager.loadTokens();

			if (tokens == null) {
				throw new IllegalStateException("User not logged in.");
			}

			String accessToken = tokens[0];
			String refreshToken = tokens[1];

			// Construct the WebSocket URI with the required query parameters
			String uri = String.format("ws://192.168.137.1:3002?type=application&feederId=%s&refreshToken=%s", deviceId, refreshToken);

			HttpClient client = HttpClient.newHttpClient();

			// Build the WebSocket connection with headers
			webSocket = client.newWebSocketBuilder().header("Authorization", "Bearer " + accessToken)
					.buildAsync(URI.create(uri), new WebSocket.Listener() {
						@Override
						public void onOpen(WebSocket webSocket) {
							System.out.println("WebSocket connected!");
							webSocket.request(1); // Request the first message
						}

						@Override
						public CompletableFuture<?> onText(WebSocket webSocket, CharSequence data, boolean last) {

							// Pass the received data to the UI updater
							if (UserManager.feederWebEventListener != null) {
								try {
									// Pass the received data to the UI updater
									// Update UI on the JavaFX thread
									Platform.runLater(() -> {
										if (UserManager.feederWebEventListener != null) {
											UserManager.feederWebEventListener.onMessageReceived(data.toString());
										}
									});
								} catch (Exception e) {
									throw new RuntimeException(e);
								}
							}

							webSocket.request(1); // Request the next message
							return CompletableFuture.completedFuture(null);
						}

						@Override
						public void onError(WebSocket webSocket, Throwable error) {
							System.err.println("WebSocket error: " + error.getMessage());
						}

						@Override
						public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
							System.out.println("WebSocket closed: " + reason);
							webSocket = null;
							return null;
						}
					}).join();

			// Request the first message
			webSocket.request(1);

			return true;
		} catch (Exception e) {
			System.out.println("Failed to connect to socket: " + e.getMessage());
			return false;
		}
	}

	/** Set feeder Socket Event Listener */
	public static void setFeederWebEventListener(FeederSocketEventListener feederWebEventListener) {
		UserManager.feederWebEventListener = feederWebEventListener;
	}

	/** Logout method */
	public static void logout() throws Exception {
		TokenManager.clearTokens();
	}

	/** Check if user is logged in */
	public static boolean isLoggedIn() {
		try {
			String[] tokens = TokenManager.loadTokens();

			boolean loggedIn = tokens != null && tokens[0] != null && tokens[1] != null && verifyTokens();

			if (!loggedIn) {
				TokenManager.clearTokens();
			}

			return loggedIn;
		} catch (Exception error) {
			System.out.println("Failed to send request to server: " + error.getMessage());
			return false;
		}
	}
}

