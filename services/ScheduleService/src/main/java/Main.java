package main.java;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import main.java.mqtt.MQTTPublisher;
import main.java.mqtt.MQTTSubscriber;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    public static void main(String[] args) {
        try {
        
            // Keep the client running to listen for incoming messages
            new MQTTSubscriber();
            // Keep the client running to listen for incoming messages

            new MQTTPublisher();
            
            Thread.sleep(5000);
        
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    
}
