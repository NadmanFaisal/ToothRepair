package main.java.mqtt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.db.ClinicSchema;
import main.java.service.ClinicService;

@Component
public class MQTT implements MqttCallback {
    private static final String BROKER_URL = "tcp://test.mosquitto.org";
    private static final String CLIENT_ID = "ClinicClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "test/clinicList";
    private final ClinicService clinicService; // CRUD Operations for the clinic database
    private static final String[] SUBSCRIBED_ALERTS = {"test/clinicAlert", "dentist/clinicAlert"}; 
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private final IMqttClient middleware; // MQTT client

    /**
     * MQTT class Constructor
     * Initializes the MQTT client, connects to the broker and subscribes to the topics.
     * 
     * @param clinicService Connection to the database, where it interacts withe the MongoDB database using CRUD operators
     * @throws MqttException throws a RuntimeException showing the error
     */

    @Autowired
    public MQTT(ClinicService clinicService){
        try {
            this.threadPool = Executors.newFixedThreadPool(SUBSCRIBED_ALERTS.length);
            this.clinicService = clinicService;
            middleware = new MqttClient(BROKER_URL, CLIENT_ID);
            middleware.connect();
            middleware.setCallback(this);
            this.subscribeToAlerts();
        } catch (MqttException e) {
            throw new RuntimeException("Failed to initialize MQTT client", e);
        }
    }

     /**
     * Subscribes to the topic by assigning a thread to subscribe to that topic.
     * 
     * Subscription happening with QoS 1.
     * 
     * It subscribes while the client is connected within a 1 second interval
     * 
     * @param N/A no params needed
     * @throws InterruptedException prints Error Stack trace
     */
    private void subscribeToAlerts() {
        for (String topic : SUBSCRIBED_ALERTS) {
            threadPool.submit(()-> {
                try {
                    if (middleware.isConnected()) {
                        middleware.subscribe(topic, 0); //Subscribe to topic
                    } else {
                        System.out.println("ClientService is not connected to the broker. Cannot subscribe to topic: " + topic);
                    }
                } catch (Exception e) {
                    System.out.println("Failed to subscribe to topic " + topic + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
        try {
            Thread.sleep(1000); // 1 second interval
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    private void subscribeToTopic(String topic) {
        threadPool.submit(()-> {
            try {
                if (middleware.isConnected()) {
                    middleware.subscribe(topic, 0);
                } else {
                    System.out.println("ClientService is not connected to the broker. Cannot subscribe to topic: " + topic);
                }
            } catch (Exception e) {
                System.out.println("Failed to subscribe to topic " + topic + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Publishes the topic as a String in JSON notation.
     * 
     * Publishing happening with QoS 1.
     * 
     * It publishes with the conected client
     * 
     * @param N/A no params needed
     * @throws MqttException prints the Error Stack trace
     */
    private void publishClinicList(){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String clinicListJson = objectMapper.writeValueAsString(this.clinicService.getAllClinics());
            String emptyMessage = "";
            //Publish the payload as bytes to the topic.
            
            middleware.publish(PUBLISHED_TOPIC, clinicListJson.getBytes(), 1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

    public void createClinic(ClinicSchema clinicInformation) {
        System.out.println("ClinicInfo has been saved into the database: " + clinicInformation);
        this.clinicService.createClinic(clinicInformation);
    }

    /**
     * Publishes the topic as a String in JSON notation.
     * 
     * Publishing happening with QoS 1.
     * 
     * It publishes with the conected client
     * 
     * @param N/A no params needed
     * @throws MqttException prints the Error Stack trace
     */
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost: " + cause.getMessage());
        while(!middleware.isConnected()){
        try {
            System.out.println("Attempting to reconnect...");
            middleware.reconnect();
            this.subscribeToAlerts();
        } catch (Exception e) {
            System.err.println("Reconnection failed. Retrying...");
            cause.printStackTrace();
        }
    }
    System.out.println("Sucessfully Reconnected to the Broker");
        
    }


    /**
     * Logic to check which topic it is subscribed from to call which method.
     * 
     * @param topic MQTT topic it came from
     * @param message payload of the following MQTT topic
     * @throws Exception prints the Error Stack trace
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        
        try {
            String stringMessage = new String(message.getPayload()); 
                   
            System.out.println("Message recieved: " + stringMessage + "\nTopic: " + topic);
            if (topic.equals("test/clinicAlert")) {
                handleClinicAlert(stringMessage);
            } else if (topic.equals("test/createClinic") ) {
                handleCreateClinic(stringMessage);
            } else if (topic.equals("dentist/clinicAlert")) {
                handleDentistAlert(stringMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClinicAlert(String message) {
        if(message.equals("Get Clinics")){
            this.publishClinicList();
        } else if (message.equals("Subscribe To Clinic Info Topic")) {
            this.subscribeToTopic("test/createClinic");
        }
    }
    private void handleDentistAlert(String message) {
        if(message.equals("Recieve Dentist")) {
            this.subscribeToTopic("dentist/clinicService/addDentist");
        }
    }

    private void handleCreateClinic(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ClinicSchema clinicInfo = objectMapper.readValue(message, ClinicSchema.class);
            clinicService.createClinic(clinicInfo);
            
        } catch (Exception e) {
            System.err.println("Error creating clinic: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Delivery Method which shows that the delivery has been completed 
     * 
     * @param token monitor status of published message to ensure it has been successfully delivered
     * @throws MqttException print Error Stack trace
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    try {
        System.out.println("Delivery complete for message: " + token.getMessage());
    } catch (MqttException e) {
        e.printStackTrace();
    }
    
    }
}
