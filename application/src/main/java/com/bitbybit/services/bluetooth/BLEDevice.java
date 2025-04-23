package com.bitbybit.services.bluetooth;

public class BLEDevice {
    private final String name;
    private final String id;

    public BLEDevice(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}