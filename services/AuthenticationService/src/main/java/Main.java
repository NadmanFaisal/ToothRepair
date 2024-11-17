package main.java;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import main.java.mqtt.MQTTPublisher;
import main.java.mqtt.MQTTSubscriber;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        try {
            SpringApplication.run(Main.class, args);
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
