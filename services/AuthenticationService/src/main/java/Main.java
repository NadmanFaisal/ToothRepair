package main.java;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import main.java.mqtt.MQTT;
import main.java.service.PatientService;


@SpringBootApplication
public class Main {
    private static PatientService patientService;
    
        public static void main(String[] args) {
    
                SpringApplication.run(Main.class, args);
                
                
                /* 
                context.getBean(MQTT.class);
                try {
                    new MQTT(patientService);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                */
               
    }
    
}
