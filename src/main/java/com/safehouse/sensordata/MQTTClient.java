package com.safehouse.sensordata;

import java.time.LocalTime;
import org.eclipse.paho.client.mqttv3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;

public class MQTTClient {
    public static void main(String[] args)  {
        String publisherId = "1";
        IMqttClient publisher= null;
        try {
             publisher = new MqttClient("tcp://iot.eclipse.org:1883", publisherId);
        } catch (MqttException e){

        }
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        //publisher.connect(options);
        SensorData sd= new SensorData(publisher);
        sd.start();
        try {
            sd.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class SensorData extends Thread{

    //private static final Logger log = LoggerFactory.getLogger(EngineTemperatureSensor.class);
    //public static final String TOPIC = "esg31/sensor_data";

    private IMqttClient client;
    private Random rnd = new Random();

    public SensorData(IMqttClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        /*
        if ( !client.isConnected()) {
            log.info("[I31] Client not connected.");
            return null;
        }
        */
        MqttMessage msg = formMessage();
        msg.setQos(0);
        msg.setRetained(true);
        //client.publish(TOPIC,msg);
    }

    /**
     * This method simulates reading the engine temperature
     * @return
     */
    private MqttMessage formMessage() {
        ObjectMapper mapper = new ObjectMapper();
        Sensor person = new Sensor("1", 10 + rnd.nextDouble() * 10.0, 5+ rnd.nextDouble() * 90.0,50+ rnd.nextDouble() * 100.0 ,System.currentTimeMillis());
        String jsonString= "";
        try {
            jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(person);
            System.out.println(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] payload = jsonString.getBytes();
        MqttMessage msg = new MqttMessage(payload);
        return msg;
    }
}
