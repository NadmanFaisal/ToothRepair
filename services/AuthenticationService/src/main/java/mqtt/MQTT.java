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

import main.java.db.DentistSchema;
import main.java.db.PatientSchema;
import main.java.service.DentistService;
import main.java.service.PatientService;

@Component
public class MQTT implements MqttCallback {
    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "AuthenticationServiceClient";      // Unique client ID
    private static final String PUBLISHED_STATUS_TOPIC = "authentication/status";
    private static final String PUBLISHED_PATIENT_TOPIC = "authentication/patientList";
    
    private final PatientService patientService; // CRUD Operations for the patient database
    private final DentistService dentistService; // CRUD Operations for the dentist  database
    private static final String[] SUBSCRIBED_TOPICS = { "test/patientAlert", "patient/authentication/signup", "dentist/authentication/signup"};
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private final IMqttClient middleware; // MQTT client

    /**
     * MQTT class Constructor
     * Initializes the MQTT client, connects to the broker and subscribes to the topics.
     * 
     * @param patientSerivce Connection to the database, where it interacts withe the MongoDB database using CRUD operators
     * @throws MqttException throws a RuntimeException showing the error
     */

    @Autowired
    public MQTT(PatientService patientService, DentistService dentistService){
        try {
            this.threadPool = Executors.newFixedThreadPool(SUBSCRIBED_TOPICS.length);
            this.patientService = patientService;
            this.dentistService = dentistService;
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
            for (String topic : SUBSCRIBED_TOPICS) {
                threadPool.submit(()-> {
                    try {
                        if(middleware.isConnected()){
                            middleware.subscribe(topic, 0); //Subscribe to topic
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            try {
                Thread.sleep(1000); // 1 second interval
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    /**
     * Publishes all patients in the patient topic as a String in JSON notation.
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
            middleware.publish(PUBLISHED_PATIENT_TOPIC, patientListJson.getBytes(), 0, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reconnects to the client and subscribes to the topics
     * 
     * @param cause to throw the error stack trace
     * @throws Exception prints the Error Stack trace
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
            String stringMessage = new String(message.getPayload());
            ObjectMapper objectMap = new ObjectMapper();

            if(stringMessage.equals("Get Patients")){
                System.out.println("Message recieved: " + stringMessage);
                this.publishPatientList();
            } else if(topic.equals(SUBSCRIBED_TOPICS[1])){
                System.out.println("Message recieved: " + stringMessage);
                PatientSchema patient = objectMap.readValue(stringMessage, PatientSchema.class);
                if(!patientService.checkDuplicatePatient(patient)){
                    System.out.println(!patientService.checkDuplicatePatient(patient));
                    patientService.createPatient(patient);
                    //middleware.unsubscribe(SUBSCRIBED_TOPICS[1]);
                }else{
                    String errorMessage = "Error: An account with this email already exists";
                    System.out.println(errorMessage);
                    middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 0, false);
                    System.out.println("has published");
                    //middleware.unsubscribe(SUBSCRIBED_TOPICS[1]);
                }

            } else if(topic.equals(SUBSCRIBED_TOPICS[2])){
                System.out.println("Message recieved: " + stringMessage);
                DentistSchema dentist = objectMap.readValue(stringMessage, DentistSchema.class);
                if(!dentistService.checkDuplicateDentist(dentist)){
                    dentistService.createDentist(dentist);
                    //middleware.unsubscribe(SUBSCRIBED_TOPICS[2]);
                }else{
                    String errorMessage = "Error: An account with this email already exists";
                    System.out.println(errorMessage);
                    middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 0, false);
                    //middleware.unsubscribe(SUBSCRIBED_TOPICS[2]);
                }
                
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
