package main.java.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    public static void main(String[] args) {
        try {
        

            // Keep the client running to listen for incoming messages
            MQTTSubscriber mqttServices = new MQTTSubscriber();
            mqttServices.connect();
            mqttServices.subscribe();
            // Keep the client running to listen for incoming messages

            MQTTPublisher mqttService = new MQTTPublisher();
            mqttService.connect();
            mqttService.publish("Hello this is the Authentication Service");
            
            Thread.sleep(5000);
        

            mqttService.disconnect();

            mqttService.disconnect();
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }

    }
    
}
