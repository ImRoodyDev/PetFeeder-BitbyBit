package com.bitbybit.animations;

import javafx.animation.ScaleTransition;
import javafx.scene.Node; // General type for all UI elements
import javafx.util.Duration;

public class ScaleAnimation {
    private static ScaleTransition scaleAnimation;

    // Initialize the scale animation
    private static void setupScaleAnimation(Node item, double scaleTo, double animationMs) {
        scaleAnimation = new ScaleTransition(Duration.seconds(animationMs), item);
        scaleAnimation.setByX(scaleTo);
        scaleAnimation.setByY(scaleTo);
        scaleAnimation.setCycleCount(ScaleTransition.INDEFINITE); // Repeat indefinitely
        scaleAnimation.setAutoReverse(true); // Reverse the scaling
    }

    // Start the scale animation
    public static void startScaler(Node item, double scaleTo, double animationMs) {
        setupScaleAnimation(item, scaleTo, animationMs);

        if (scaleAnimation != null) {
            scaleAnimation.play();
        }
    }

    // Stop the scale animation
    public static void stopScaler() {
        if (scaleAnimation != null) {
            scaleAnimation.stop();
            scaleAnimation = null;
        }
    }
}
