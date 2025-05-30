package com.bitbybit.services.authentication;

import java.nio.file.*;
import java.io.IOException;

public class TokenManager {
	private static final String TOKEN_FILE = "tokens.txt";

	public static void saveTokens(String accessToken, String refreshToken) throws IOException {
		String tokens = "accessToken=" + accessToken + "\nrefreshToken=" + refreshToken;
		Files.writeString(Path.of(TOKEN_FILE), tokens);
	}

	public static String[] loadTokens() throws IOException {
		if (!Files.exists(Path.of(TOKEN_FILE))) {
			return null;
		}
		String content = Files.readString(Path.of(TOKEN_FILE));
		String accessToken = content.split("\n")[0].split("=")[1];
		String refreshToken = content.split("\n")[1].split("=")[1];
		return new String[]{accessToken, refreshToken};
	}

	public static void clearTokens() throws IOException {
		Files.deleteIfExists(Path.of(TOKEN_FILE));
	}
}
