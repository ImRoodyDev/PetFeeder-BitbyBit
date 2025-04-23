package com.bitbybit.services.authentication;

public interface FeederSocketEventListener {
	void onMessageReceived(String message);

	void onClose();
}
