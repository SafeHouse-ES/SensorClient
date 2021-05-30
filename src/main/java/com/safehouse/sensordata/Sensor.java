package com.safehouse.sensordata;

public class Sensor {
    private String id;
    private double temperature;
    private double luminosity;
    private boolean movement;
    private long timestamp;

    public Sensor(String id, double temperature, double luminosity, boolean movement, long timestamp) {
        this.id = id;
        this.temperature = temperature;
        this.luminosity = luminosity;
        this.movement = movement;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getLuminosity() {
        return luminosity;
    }

    public boolean getMovement() {
        return movement;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
