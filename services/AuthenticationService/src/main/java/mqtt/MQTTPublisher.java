package main.java.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.service.PatientService;
import main.java.db.PatientRepository;
@Component
public class MQTTPublisher {

    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "test/patientList";
    private final PatientService patientService;

    //Put them in a JSON format for the payload. (might be useful in the future)
    //private static final String userWeightAndHeight = "{"userWeight": "  + userWeight + "," + " "userHeight": " + userHeight + "}";
    @Autowired
    public MQTTPublisher(PatientService patientService) {
        this.patientService = patientService;
        publishPatientList();
    }

    //Note: Needs to publish after waiting for a set amount of time (probably done in the spring boot main file)
    private void publishPatientList(){
        try {
             
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.connect();
            System.out.println("MQTTPublisher has been connected to: " + PUBLISHED_TOPIC);


            ObjectMapper objectMapper = new ObjectMapper();
            String patientListJson = objectMapper.writeValueAsString(this.patientService.getAllPatients());

            //Publish the payload as bytes to the topic.
            client.publish(PUBLISHED_TOPIC, patientListJson.getBytes(), 0, false);
            System.out.println(this.patientService.getAllPatients().toString()+ "Has been published");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

}