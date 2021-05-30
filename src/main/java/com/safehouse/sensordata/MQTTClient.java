package com.safehouse.sensordata;

import org.eclipse.paho.client.mqttv3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public class MQTTClient {
    public static void main(String[] args) throws MqttException {
        String publisherId = "1";
        IMqttClient publisher= null;
        try {
             publisher = new MqttClient("tcp://192.168.160.18:1883", publisherId);
        } catch (MqttException e){

        }
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        publisher.connect(options);
        SensorData sd0= new SensorData(publisher, "Kitchen");
        SensorData sd1= new SensorData(publisher, "LivingRoom");
        SensorData sd2= new SensorData(publisher, "Room1");
        SensorData sd3= new SensorData(publisher, "Room2");
        sd0.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sd1.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sd2.start();
        try {
            Thread.sleep(2000);
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

class SensorData extends Thread{

    public static final String TOPIC = "es31_sensordata";

    private IMqttClient client;
    private Random rnd = new Random();
    private String sensorId;

    public SensorData(IMqttClient client, String sensorId) {
        this.client = client;
        this.sensorId= sensorId;
    }

    @Override
    public void run() {

        if ( !client.isConnected()) {
            return ;
        }
        while (true) {
            MqttMessage msg = formMessage();
            msg.setQos(0);
            msg.setRetained(true);
            try {
                client.publish(TOPIC, msg);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method simulates reading the engine temperature
     * @return
     */
    private MqttMessage formMessage() {
        ObjectMapper mapper = new ObjectMapper();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Sensor sensor = new Sensor(sensorId, getTemperature(cal) , getLuminosity(cal), getMovement(cal), cal.getTimeInMillis());
        byte[] payload= null;
        try {
            payload = mapper.writeValueAsBytes(sensor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MqttMessage msg = new MqttMessage(payload);
        return msg;
    }

    private double getTemperature(Calendar cal){
        double temp= 0;
        int minutes= cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        temp = 20.5137 - 0.0341183 * (Math.pow(Math.abs(minutes-720),0.874271))+ Math.random()*2;
        return temp;
    }

    private double getLuminosity(Calendar cal){
        double lum= 0;
        int minutes= cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        if (minutes > 360 && minutes < 1320){
            lum = 517.419 - 7.21052 * (Math.pow(Math.abs(minutes-840),0.684096)) + Math.random()*5;
        }
        return lum;
    }

    private boolean getMovement(Calendar cal){
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
