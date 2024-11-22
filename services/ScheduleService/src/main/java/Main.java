package main.java;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import main.java.mqtt.MQTTPublisher;
import main.java.mqtt.MQTTSubscriber;


@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        context.getBean(MQTTSubscriber.class);
        context.getBean(MQTTPublisher.class);
    }
    
}
