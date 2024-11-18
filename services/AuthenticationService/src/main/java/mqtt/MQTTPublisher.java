package main.java.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private void publishPatientList(){
        try {
             
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.connect();
            System.out.println("MQTTPublisher has been connected!");

            //Publish the payload as bytes to the topic.
            client.publish(PUBLISHED_TOPIC, this.patientService.getAllPatients().toString().getBytes(), 0, false);
            System.out.println(this.patientService.getAllPatients().toString()+ "Has been published");
            

        } catch (MqttException e) {
            e.printStackTrace();
        }
    } 

}