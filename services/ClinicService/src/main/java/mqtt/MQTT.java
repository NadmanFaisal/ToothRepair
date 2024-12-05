package main.java.mqtt;

import java.util.Map;
import java.util.Optional;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.db.ClinicSchema;
import main.java.service.ClinicService;

@Component
public class MQTT implements MqttCallback {
    private static final String BROKER_URL = "tcp://test.mosquitto.org";
    private static final String CLIENT_ID = "ClinicClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC_CLIENT = "test/clinicList";
    private static final String PUBLISHED_TOPIC_DENTIST = "clinicService/clinicList";

    private final ClinicService clinicService; // CRUD Operations for the clinic database
    private static final String[] SUBSCRIBED_TOPICS = {"test/clinicAlert", "dentist/clinicService/addDentist", "test/createClinic", "dentist/clinicService/alert"}; 
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private final IMqttClient middleware; // MQTT client
    private ObjectMapper objectMapper = new ObjectMapper();


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
            this.threadPool = Executors.newFixedThreadPool(SUBSCRIBED_TOPICS.length);
            this.clinicService = clinicService;
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
     * Subscription happening with QoS 0.
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
                    if (middleware.isConnected()) {
                        middleware.subscribe(topic, 1); //Subscribe to topic
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
    private void publishClinicList(String topic){
        try {
            String clinicListJson = objectMapper.writeValueAsString(this.clinicService.getAllClinics());
            
            middleware.publish(topic, clinicListJson.getBytes(), 2, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

    /**
     * Saves the clinic information into the database
     * 
     * @param clinicInformation the data of the new clinic
     */
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
                   
            System.out.println("Message recieved: " + stringMessage + " Topic: " + topic);
            switch (topic) {
                case "test/clinicAlert":
                    handleClinicAlert(stringMessage, PUBLISHED_TOPIC_CLIENT);
                    break;
                case "dentist/clinicService/alert":
                    handleClinicAlert(stringMessage, PUBLISHED_TOPIC_DENTIST);
                    break;
                case "test/createClinic":
                    handleCreateClinic(stringMessage);
                    break;
                case "dentist/clinicService/addDentist":
                    handleAddingDentist(stringMessage);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the payload from the 'test/clinicAlert' topic
     * 
     * @param message payload of the 'test/clinicAlert' topic
     */
    private void handleClinicAlert(String message, String topic) {
        if(message.equals("Get Clinics")){
            this.publishClinicList(topic);
        }
    }

    /**
     * If the clinic information is not empty, saves it into the mongodb database
     * 
     * @param message  payload of the 'test/createClinic' topic
     * @throws Exception prints the Error Stack Trace
     */
    private void handleCreateClinic(String message) {
        try {
            ClinicSchema clinicInfo = objectMapper.readValue(message, ClinicSchema.class);
            
            if (checkClinicInfo(clinicInfo)) {
                clinicService.createClinic(clinicInfo);
            } else {
                System.err.println("Some clinic information in the payload is missing");
            }
            
        } catch (Exception e) {
            System.err.println("Error creating clinic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the recieved clinicInfo has values in the corresponding attributes.
     * 
     * @param clinicInfo the clinicInfo 
     * @return returns true if the clinicInfo has all the neccessary information
     */
    public boolean checkClinicInfo(ClinicSchema clinicInfo) {
        boolean clinicInfoExists = true;
        if (clinicInfo.getAddress().isEmpty()) {
            System.err.println("Clinic address was not given in the payload");
            clinicInfoExists = false;
        } else if (clinicInfo.getName().isEmpty()) {
            System.err.println("Clinic name was not given in the payload");
            clinicInfoExists = false;
        } else if (clinicInfo.getOpenHours().isEmpty()) {
            System.err.println("Clinic open hours was not given in the payload");
        } else if (clinicInfo.getContactInfo().getEmail().isEmpty()) {
            System.err.println("Clinic email was not given in the payload");
            clinicInfoExists = false;
        } else if (clinicInfo.getContactInfo().getNumber().isEmpty()){
            System.err.println("Clinic number was not given in the payload");
            clinicInfoExists = false;
        } else if (clinicInfo.getCoordinate() == null) {
            System.err.println("Clinic coordinates was not given in the payload");
            clinicInfoExists = false;
        }
        return clinicInfoExists;
    }
    /**
     * Finds the clinic with the id provided and adds the reference to the dentist to it
     * 
     * @param message payload of the 'dentist/clinicService/addDentist' topic
     * @throws Exception prints Error Stack Trace
     */
    private void handleAddingDentist(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> messageData = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});
            
            String clinicId = messageData.get("clinicId");
            String dentistId = messageData.get("dentistId");
            
            if (clinicId.isEmpty()) {
                System.out.println("No clinic Id was provided");
                return;
            } else if (dentistId.isEmpty()) {
                System.out.println("No dentist Id was provided");
                return;
            }

            Optional<ClinicSchema> updatedClinic = clinicService.addDentist(clinicId, dentistId);

            if (updatedClinic.isPresent()) {
               System.out.println("Dentist has been added to clinic: " + clinicId);
            } else {
                System.out.println("Clinic not found with Id: " + clinicId);
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
