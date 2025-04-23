package com.bitbybit.animations;

import javafx.animation.RotateTransition;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class SpinnersAnimation {
    private static RotateTransition spinnerAnimation;

    // Initialize the spinner animation
    private static void setupSpinnerAnimation(SVGPath spinner) {
        spinnerAnimation = new RotateTransition(Duration.seconds(0.8), spinner);
        spinnerAnimation.setByAngle(360); // Rotate full circle
        spinnerAnimation.setCycleCount(RotateTransition.INDEFINITE); // Repeat indefinitely
    }

    // Start the spinner animation
    public static void startSpinner(SVGPath spinner) {
        setupSpinnerAnimation(spinner);

        if (spinnerAnimation != null) {
            spinnerAnimation.play();
        }
    }

    // Stop the spinner animation
    public static void stopSpinner() {
        if (spinnerAnimation != null) {
            spinnerAnimation.stop();
            spinnerAnimation = null;
        }
    }


}
