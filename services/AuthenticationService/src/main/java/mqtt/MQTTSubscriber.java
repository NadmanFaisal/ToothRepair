package main.java.mqtt;

import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import main.java.controller.PatientController;
import main.java.db.PatientSchema;

@Component
public class MQTTSubscriber {

    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String SUBSCRIBED_TOPIC = "test/Authentication";

    @Autowired
    private PatientController patientController;
   
    //Note: Redo this file, would make sense for subscriber to always be subscribed to topic since start of program
    public MQTTSubscriber() {
        try {
            // Initialise MQTT client and connect to broker using broker and client id
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            MqttConnectOptions connection = new MqttConnectOptions();
            client.connect(connection);
            System.out.println("Connected");

            // Defines what will happen when the connection is lost, message is arrived and delivery is completed
            client.setCallback(new MqttCallback() {
                // Method to show in case connection is lost
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection is lost");
                }

                // Method to save message from the MQTT broker into the JSON file
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Message recieved: " + message.toString());
                    
                    if (topic.equals(SUBSCRIBED_TOPIC)) {
                        List<PatientSchema> allPatients = patientController.getAllPatients();

                        StringBuilder patientListResponse = new StringBuilder();
                        for (PatientSchema patient: allPatients) {
                            patientListResponse
                                .append("ID: ").append(patient.getId())
                                .append(", Name: ").append(patient.getName()).append("\n");
                        }
                        MqttMessage responseMessage = new MqttMessage(patientListResponse.toString().getBytes());
                        client.publish("test/patientList", responseMessage);
                        System.out.println("Patient list sent to response topic");
                    }

                }

                // Method to see that delivery is completed
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Complete");
                }
            });
            // Subscribe to different topics to receive message
            client.subscribe(SUBSCRIBED_TOPIC);
            System.out.println("Subscribed to topic: " + SUBSCRIBED_TOPIC);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed");
        }
    }
    
}