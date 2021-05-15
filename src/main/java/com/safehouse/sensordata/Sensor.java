package com.safehouse.sensordata;

public class Sensor {
    private String id;
    private double temperature;
    private double humidity;
    private double luminosity;
    private long timestamp;

    public Sensor(String id, double temperature, double humidity, double luminosity, long timestamp) {
        this.id = id;
        this.temperature = temperature;
        this.humidity = humidity;
        this.luminosity = luminosity;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getLuminosity() {
        return luminosity;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
