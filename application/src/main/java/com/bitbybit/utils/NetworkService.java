package com.bitbybit.utils;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.stream.Collectors;

public class NetworkService {
	private static final CloseableHttpClient httpClient = HttpClients.createDefault();

	// Get the local machine's IP address (change this if you're passing real user IP)
	private static final String localIp = "80.115.232.59";

	// POST request with access token in header and refresh token as a cookie
	public static JSONObject post(String url, Map<String, String> data, String accessToken, String refreshToken) throws Exception {
		String json = mapToJson(data);

		System.out.println("\n\nJSON Payload: " + json);

		HttpPost postRequest = new HttpPost(url);
		postRequest.setHeader("Content-Type", "application/json");

		// Add the IP address as a custom header
		postRequest.addHeader("X-Client-IP", localIp);

		if (accessToken != null && !accessToken.isEmpty()) {
			postRequest.setHeader("Authorization", "Bearer " + accessToken);
		}

		if (refreshToken != null && !refreshToken.isEmpty()) {
			postRequest.setHeader("Cookie", "refreshToken=" + refreshToken);
		}

		postRequest.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

		try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
			// Read the response body and status code
			String responseBody = readResponse(response);
			int statusCode = response.getCode();

			System.out.println("Response body: " + responseBody);
			System.out.println("Response Code: " + statusCode);


			JSONObject jsonResponse = new JSONObject(responseBody);
			jsonResponse.put("statusCode", statusCode);

			if (response.getHeader("x-authentication") != null)
				jsonResponse.put("accessToken", response.getHeader("x-authentication"));

			// Initialize a variable to hold the refresh token
			String newRefreshToken = null;

			// Extract cookies from the response headers
			org.apache.hc.core5.http.Header[] cookies = response.getHeaders("Set-Cookie");
			for (org.apache.hc.core5.http.Header cookie : cookies) {
				String cookieValue = cookie.getValue();
				System.out.println("Extracted Refresh Token: " + cookieValue);
				if (cookieValue.startsWith("refreshToken=")) {
					newRefreshToken = cookieValue.split(";")[0].substring("refreshToken=".length());
					jsonResponse.put("refreshToken", newRefreshToken);
					break; // Stop once the refreshToken is found
				}
			}

			return jsonResponse;
		}
	}

	// GET request with access token in header and refresh token as a cookie
	public static JSONObject get(String url, String accessToken, String refreshToken) throws Exception {
		HttpGet getRequest = new HttpGet(url);

		// Add the IP address as a custom header
		getRequest.addHeader("X-Client-IP", localIp);

		if (accessToken != null && !accessToken.isEmpty()) {
			getRequest.setHeader("Authorization", "Bearer " + accessToken);
		}

		if (refreshToken != null && !refreshToken.isEmpty()) {
			getRequest.setHeader("Cookie", "refreshToken=" + refreshToken);
		}

		try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
			// Read the response body and status code
			String responseBody = readResponse(response);
			int statusCode = response.getCode();

			System.out.println("\n\nRequest URL: " + url);
			System.out.println("Response body: " + responseBody);
			System.out.println("Response Code: " + statusCode);

			JSONObject jsonResponse = new JSONObject(responseBody);
			jsonResponse.put("statusCode", statusCode);

			return jsonResponse;
		}
	}

	// Helper method to read the response body
	private static String readResponse(CloseableHttpResponse response) throws Exception {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
			return reader.lines().collect(Collectors.joining("\n"));
		}
	}

	// Helper to convert Map to JSON string
	private static String mapToJson(Map<String, String> data) {
		return data.entrySet().stream()
				.map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue().replace("\"", "\\\"") + "\"")
				.collect(Collectors.joining(",", "{", "}"));
	}

	// Function to get the local IP address (for testing)
	private static String getLocalIp() {
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			return inetAddress.getHostAddress(); // Return the local machine IP
		} catch (UnknownHostException e) {
			return "";
		}
	}
}
