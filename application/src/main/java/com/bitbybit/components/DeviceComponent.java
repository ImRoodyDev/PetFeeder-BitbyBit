package com.bitbybit.components;

import com.jfoenix.controls.JFXRadioButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;

public class DeviceComponent {
    public final String deviceName;
    public final String deviceMac;
    private final Node element;

    @FXML
    private JFXRadioButton deviceRadioButton;

    @FXML
    private Label deviceLabel;

    public DeviceComponent(String name, String mac, ToggleGroup toggleGroup) throws IOException {
        deviceName = name;
        deviceMac = mac;

        FXMLLoader root = new FXMLLoader(getClass().getResource("/components/device-item-component.fxml"));
        root.setController(this);
        element = root.load();
        deviceRadioButton.setText(deviceMac);
        deviceLabel.setText(deviceName);
        deviceRadioButton.setToggleGroup(toggleGroup);
    }

    public Node getNodeComponent() {
        return element;
    }

    public void onAction() {
        System.out.println("Selected device: " + deviceName);
    }

}
