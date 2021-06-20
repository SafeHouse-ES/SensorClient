package com.safehouse.sensordata;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.paho.client.mqttv3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.io.InputStreamReader;

public class MQTTClient {

    public static void main(String[] args) throws MqttException {
        String publisherId = "1";
        IMqttClient publisher= null;
        try {
             publisher = new MqttClient("tcp://192.168.160.18:1883", publisherId);
        } catch (MqttException e){

        }
        PublishersSR publishersSR= new PublishersSR();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        publisher.connect(options);
        SensorData sd0= new SensorData(publisher, "Kitchen", publishersSR);
        SensorData sd1= new SensorData(publisher, "LivingRoom", publishersSR);
        SensorData sd2= new SensorData(publisher, "Room1", publishersSR);
        SensorData sd3= new SensorData(publisher, "Room2", publishersSR);
        sd0.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sd1.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sd2.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sd3.start();
        try {
            sd0.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            sd1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            sd2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            sd3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class PublishersSR {

    private boolean free = true;

    public static final String TOPIC = "es31_sensordata";

    public synchronized void publishMessage(IMqttClient client, String sensorId){
        while(!free) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        free= false;
        MqttMessage msg = formMessage(sensorId);
        msg.setQos(0);
        msg.setRetained(true);
        try {
            client.publish(TOPIC, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        free= true;
        notifyAll();
    }

    /**
     * This method simulates reading the engine temperature
     * @return
     */
    private MqttMessage formMessage(String sensorId) {
        ObjectMapper mapper = new ObjectMapper();
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
        Sensor sensor = new Sensor(sensorId, getTemperature(cal, sensorId) , getLuminosity(cal, sensorId), getMovement(cal, sensorId), Instant.now().toEpochMilli());
        byte[] payload= null;
        try {
            payload = mapper.writeValueAsBytes(sensor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MqttMessage msg = new MqttMessage(payload);
        return msg;
    }

    private double getTemperature(Calendar cal, String sensorId) {
        double temp= 0;
        int minutes= cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        temp = 20.5137 - 0.0341183 * (Math.pow(Math.abs(minutes-720),0.874271))+ Math.random() - 0.5;
        double actemp= 0;

        URL url = null;
        try {
            url = new URL("http://192.168.160.87:31006/device?id="+sensorId+"&dev=ac");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;

            while ((output = br.readLine()) != null) {
                if (!output.equals("{}")) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode actualObj = mapper.readTree(output);
                    actemp = Double.parseDouble(actualObj.get("value").asText());
                }
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        if (actemp != 0.0){
            temp = (temp + actemp * 3) / 4;
        }
        return temp;
    }

    private double getLuminosity(Calendar cal, String sensorId){
        double lum= 0;
        int minutes= cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        if (minutes > 360 && minutes < 1320){
            lum = 517.419 - 7.21052 * (Math.pow(Math.abs(minutes-840),0.684096)) + Math.random()*2;
        }

        double sllum= 0;
        URL url = null;
        try {
            url = new URL("http://192.168.160.87:31006/device?id="+sensorId+"&dev=sl");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;

            while ((output = br.readLine()) != null) {
                if (!output.equals("{}")) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode actualObj = mapper.readTree(output);
                    sllum = Double.parseDouble(actualObj.get("value").asText());
                }
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (sllum != 0.0){
            lum = (lum + 500) / 2;
        }

        return lum;
    }

    private boolean getMovement(Calendar cal, String sensorId){
        boolean mov= false;
        int hours_day= cal.get(Calendar.HOUR_OF_DAY);
        switch (sensorId){
            case "Kitchen": if (hours_day == 6 || hours_day== 17 || (hours_day>=19 && hours_day <22)) mov= true;
                break;
            case "LivingRoom": if (hours_day == 18 || (hours_day>=20 && hours_day <22)) mov= true;
                break;
            case "Room1": if (hours_day == 16 || hours_day>=22 || hours_day <=6) mov= true;
                break;
            case "Room2": if (hours_day == 16 || hours_day == 18 || hours_day>=22 || hours_day <=6) mov= true;
                break;
        }
        return mov;
    }
}


class SensorData extends Thread{

    private IMqttClient client;
    private Random rnd = new Random();
    private String sensorId;
    private PublishersSR publishersSR;

    public SensorData(IMqttClient client, String sensorId, PublishersSR publishersSR) {
        this.client = client;
        this.sensorId= sensorId;
        this.publishersSR= publishersSR;
    }

    @Override
    public void run() {

        if ( !client.isConnected()) {
            return ;
        }
        while (true) {
            publishersSR.publishMessage(client, sensorId);
            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
