package main.java.mqtt;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
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
    private static final String [] BROKER_URLS = { "ssl://193a0f31e34647d9a74f1e130a9238ba.s1.eu.hivemq.cloud", "tcp://broker.hivemq.com" ,"tcp://broker.emqx.io" , "tcp://test.mosquitto.org"};    
    private static final String CLIENT_ID = "ClinicClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC_CLINICS = "clinicService/clinics/getClinicList";
    private static final String PUBLISHED_TOTAL_MSG_SENT = "clinicService/totalMsgSent";
    private static final String PUBLISHED_TOTAL_MSG_RECEIVED = "clinicService/totalMsgReceived";
    private final ClinicService clinicService; // CRUD Operations for the clinic database
    private static final String[] SUBSCRIBED_TOPICS = {"clinicService/clinic/getClinicAlert", "clinicService/dentist/addDentist", "clinicService/clinic/createClinic", "ClinicService/Clinic/getClinicById", "clinicService/totalMsgSentAlert", "clinicService/totalMsgReceivedAlert"}; 
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private MqttAsyncClient middleware; // MQTT client
    private ObjectMapper objectMapper = new ObjectMapper();
    private MqttConnectOptions options = new MqttConnectOptions();
    private int currentBrokerIndex = 0;
    private boolean STRESS_TEST_MODE = false;
    private int totalMsgReceived = 0;
    private int totalMsgSent = 0;


   
    /**
     * MQTT class Constructor
     * Initializes the MQTT client, connects to the broker and subscribes to the topics.
     * 
     * @param patientSerivce Connection to the database, where it interacts withe the MongoDB database using CRUD operators
     * @throws MqttException throws a RuntimeException showing the error
    */
    @Autowired
    public MQTT(ClinicService clinicService){
        this.threadPool = Executors.newCachedThreadPool(); // Dynamically expand thread poo
        this.clinicService = clinicService;
        if(STRESS_TEST_MODE){
            options.setUserName("Administrator");
            String passwordString = "Vaibhav12Taha";
            char[] passwordChars = passwordString.toCharArray();
            options.setPassword(passwordChars);
            options.setMaxInflight(100);
        }
        try {
            initializeClient();
        } catch (MqttException e) {
            throw new RuntimeException("Failed to initialize MQTT client", e);
        }
    }

    /**
     * Handles the logic for initializing a connection to the a public MQTT broker
     * If the current broker is not reachable, then the method tries to connect to another broker url  within the for-loop
     *
     * Method is called within the constructor to initialize the connection
     * Method also calls the subscribeToTopics function to ensure the subscription of all topics
     * 
     * @param N/A no params needed
     * @throws InterruptedException prints Error Stack trace
     */
    private void initializeClient() throws MqttException {
        System.out.println("Trying to initialize client");
        for (int i = 0; i < BROKER_URLS.length; i++) {
            
            try {
                middleware = new MqttAsyncClient(BROKER_URLS[i], CLIENT_ID);
                if(STRESS_TEST_MODE && BROKER_URLS[i].equals("ssl://193a0f31e34647d9a74f1e130a9238ba.s1.eu.hivemq.cloud")){
                    IMqttToken token = middleware.connect(options);
                    token.waitForCompletion();
                } else {
                    IMqttToken token = middleware.connect();
                    token.waitForCompletion();
                }
                System.out.println("Connecting to this broker: " + BROKER_URLS[i]);
                middleware.setCallback(this);
                this.subscribeToTopics();
                System.out.println("Connected to broker: " + BROKER_URLS[i]);
                this.currentBrokerIndex = i;
                return; // Exit the loop once connected

            } catch (MqttException e) {
                System.err.println("Failed to connect to broker: " + BROKER_URLS[i] + ". Trying next...");
                if (middleware != null && middleware.isConnected()) {
                    middleware.disconnect();
                }
            }
        }
        throw new MqttException(new Throwable("All brokers failed")); // Throw after all retries
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
                    if (middleware.isConnected()) {
                        middleware.subscribe(topic, 1); //Subscribe to topic
                        System.out.println("ClinicService subscribed to topic: " + topic);
                    } else {
                        System.out.println("ClinicService is not connected to the broker. Cannot subscribe to topic: " + topic);
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
     * Publishes the message to the topic as a String in JSON notation.
     * 
     * Publishing happening with QoS 1.
     * 
     * It publishes with the conected client
     * 
     * @param N/A no params needed
     * @throws MqttException prints the Error Stack trace
     */
    private void publishClinicInfo(String topic, String message) {
        try {
            middleware.publish(topic, message.getBytes(), 1, false);
            totalMsgSent++;
            System.out.println("Published clinic info to topic: " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publishes the topic as a String in JSON notation.
     * 
     * Publishing happening with QoS 2.
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
            totalMsgSent++;
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
     * Tries to reconnect to the current broker, if unsuccessfull it will try to connect to another broker. 
     * Afterwards it subscribes to topics
     * 
     * @param cause to throw the error stack trace
     * @throws Exception prints the Error Stack trace
     */

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost: " + cause.getMessage());
        int retryCount = 0;
        int MAX_RETRIES = 3;
        while (!middleware.isConnected()) {
            
            try {
                if(retryCount < MAX_RETRIES){
                    Thread.sleep(2000);
                    System.out.println("Attempting to reconnect...");
                    middleware.reconnect();
                    middleware.setCallback(this);
                } else {
                    retryCount = 0;
                    
                    currentBrokerIndex = (currentBrokerIndex + 1) % BROKER_URLS.length;
                    System.out.println("Switching to another broker: " + BROKER_URLS[currentBrokerIndex]);
                    
                    middleware.disconnect();
                    middleware = new MqttAsyncClient(BROKER_URLS[currentBrokerIndex], CLIENT_ID);

                    System.out.println("Trying to connect to broker: " + BROKER_URLS[currentBrokerIndex]);
                    middleware.connect();
                    middleware.setCallback(this);
                }
                
            } catch (Exception e) {
                retryCount++;
                System.out.println("Reconnection failed. Attempt " + retryCount + " of " + MAX_RETRIES);
                e.printStackTrace();
            }
        }  
        System.out.println("Successfully Reconnected to the Broker");
        this.subscribeToTopics();
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
        totalMsgReceived++;
        try {
            String stringMessage = new String(message.getPayload()); 
                   
            System.out.println("Message recieved: " + stringMessage + " Topic: " + topic);
            switch (topic) {
                case "clinicService/clinic/getClinicAlert":
                    handleClinicAlert(stringMessage, PUBLISHED_TOPIC_CLINICS);
                    break;
                case "clinicService/clinic/createClinic":
                    handleCreateClinic(stringMessage);
                    break;
                case "clinicService/dentist/addDentist":
                    handleAddingDentist(stringMessage);
                    break;
                case "ClinicService/Clinic/getClinicById":
                    handleGetClinic(stringMessage);
                    break;
                case "clinicService/totalMsgReceivedAlert" :
                    System.out.println("PUBLISHING TOTAL MESSAGES RECEIVED: " + this.totalMsgReceived);
                    String msgReceivedString = String.valueOf(totalMsgReceived);
                    middleware.publish(PUBLISHED_TOTAL_MSG_RECEIVED, msgReceivedString.getBytes() , 2, false);
                    totalMsgSent++;
                    break;
                case "clinicService/totalMsgSentAlert" :
                    totalMsgSent++;
                    System.out.println("PUBLISHING TOTAL MESSAGES SENT: " + this.totalMsgSent);                  
                    String msgSentString = String.valueOf(totalMsgSent);
                    middleware.publish(PUBLISHED_TOTAL_MSG_SENT, msgSentString.getBytes(), 2, false);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the payload from the 'ClinicService/Clinic/getClinicById' topic
     * 
     * @param message payload of the 'ClinicService/Clinic/getClinicById' topic
     */
    private void handleGetClinic(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String clinicId = message;
    
            System.out.println("Fetching clinic with ID: " + clinicId);
    
            Optional<ClinicSchema> clinic = clinicService.getClinic(clinicId);
    
            if (clinic.isPresent()) {
                String clinicJson = objectMapper.writeValueAsString(clinic.get());
                publishClinicInfo("Client/ClinicService/ClinicInfo", clinicJson);
            } else {
                System.err.println("Clinic not found with ID: " + clinicId);
            }
        } catch (Exception e) {
            System.err.println("Error in handleGetClinic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the payload from the 'test/clinicAlert' topic
     * 
     * @param message payload of the 'test/clinicAlert' topic
     */
    private void handleClinicAlert(String message, String topic) {
        if(message.contains("Get Clinics")){
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
