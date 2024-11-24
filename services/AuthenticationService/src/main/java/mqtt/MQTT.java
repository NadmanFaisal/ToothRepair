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

import main.java.service.PatientService;

@Component
public class MQTT implements MqttCallback {
    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "test/patientList";
    private final PatientService patientService; // CRUD Operations for the patient database
    private static final String SUBSCRIBED_TOPIC = "test/patientAlert"; 
    private ExecutorService thread = Executors.newSingleThreadExecutor(); // thread to handle each subscribed topic
    private final IMqttClient middleware; // MQTT client

    /**
     * MQTT class Constructor
     * Initializes the MQTT client, connects to the broker and subscribes to the topics.
     * 
     * @param patientSerivce Connection to the database, where it interacts withe the MongoDB database using CRUD operators
     * @throws MqttException throws a RuntimeException showing the error
     */

    @Autowired
    public MQTT(PatientService patientService){
        try {
            this.patientService = patientService;
            middleware = new MqttClient(BROKER_URL, CLIENT_ID);
            middleware.connect();
            middleware.setCallback(this);
            this.subscribeToTopics();
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
    private void subscribeToTopics() {
        while(middleware.isConnected()){ // while client is connected
        thread.submit(()-> {
            try {
                middleware.subscribe(SUBSCRIBED_TOPIC, 0); //Subscribe to topic
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            Thread.sleep(1000); // 1 second interval
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
    private void publishPatientList(){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String patientListJson = objectMapper.writeValueAsString(this.patientService.getAllPatients());
            //Publish the payload as bytes to the topic.
            
            middleware.publish(PUBLISHED_TOPIC, patientListJson.getBytes(), 0, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            this.subscribeToTopics();
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
            String stringMessage = message.toString(); 
                   
            System.out.println("Message recieved: " + stringMessage);
            if(stringMessage.equals("Get Patients")){
                this.publishPatientList();
            }
        } catch (Exception e) {
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
