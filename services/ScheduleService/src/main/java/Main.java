package main.java;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import main.java.mqtt.MQTT;
import main.java.service.AppointmentService;


@SpringBootApplication
public class Main {
    private static AppointmentService appointmentService;
    
    public static void main(String[] args) {
    
        SpringApplication.run(Main.class, args);

    }
    
}
