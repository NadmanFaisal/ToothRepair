package main.java.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String [] BROKER_URLS = { "tcp:test.mosquitto.org", "tcp://broker.hivemq.com", "tcp://broker.emqx.io"};
    private static final String CLIENT_ID = "AuthenticationServiceClient";      // Unique client ID
    private static final String PUBLISHED_STATUS_TOPIC = "authentication/status";
    private static final String PUBLISHED_PATIENT_TOPIC = "authentication/patientList";
    private static final String PUBLISHED_CLINIC_TOPIC = "dentist/clinicService/addDentist";
    private static final String PUBLISHED_LOGIN_TOPIC = "authentication/alert/login";
    private static final String PUBLISHED_DENTIST_TOPIC = "authentication/dentist/getDentistNames";
    private static final String PUBLISHED_USER_ID_TOPIC = "authentication/userID";
    private final PatientService patientService; // CRUD Operations for the patient database
    private final DentistService dentistService; // CRUD Operations for the dentist  database
    private static final String[] SUBSCRIBED_TOPICS = { "test/patientAlert", "patient/authentication/signup", "dentist/authentication/signup", "patient/authentication/login", "dentist/authentication/login", "authentication/dentist/getDentistNamesAlert"};
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private IMqttClient middleware; // MQTT client
    private ObjectMapper objectMapper = new ObjectMapper();
    private int currentBrokerIndex = 0;

    /**
     * MQTT class Constructor
     * Initializes the MQTT client, connects to the broker and subscribes to the topics.
     * 
     * @param patientSerivce Connection to the database, where it interacts withe the MongoDB database using CRUD operators
     * @throws MqttException throws a RuntimeException showing the error
     */

    @Autowired
    public MQTT(PatientService patientService, DentistService dentistService){
        this.threadPool = Executors.newCachedThreadPool(); // Dynamically expand thread poo
        this.patientService = patientService;
        this.dentistService = dentistService;
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
                middleware = new MqttClient(BROKER_URLS[i], CLIENT_ID);
                middleware.connect();
                System.out.println("Connecting to this broker: " + BROKER_URLS[i]);
                middleware.setCallback(this);
                this.subscribeToTopics();
                System.out.println("Connected to broker: " + BROKER_URLS[i]);
                this.currentBrokerIndex = i;
                return; // Exit the loop once connected
            } catch (MqttException e) {
                System.err.println("Failed to connect to broker: " + BROKER_URLS[i] + ". Trying next...");
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
                        if(middleware.isConnected()){
                            middleware.subscribe(topic, 1); //Subscribe to topic
                            System.out.println("AuthenticationService subscribed to topic: " + topic);
                        } else {
                            System.out.println("AuthenticationService is not connected to the broker. Cannot subscribe to topic: " + topic);
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
                    middleware.setCallback(this); // Ensure callback is re-applied
                } else {
                    retryCount = 0;
                    
                    currentBrokerIndex = (currentBrokerIndex + 1) % BROKER_URLS.length;
                    System.out.println("Switching to another broker: " + BROKER_URLS[currentBrokerIndex]);
                    
                    middleware.disconnect();
                    middleware = new MqttClient(BROKER_URLS[currentBrokerIndex], CLIENT_ID);

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
        
        try {
            String stringMessage = new String(message.getPayload());
            System.out.println("Message recieved: " + stringMessage + " Topic: " + topic);

            switch (topic) {
                case "patient/authentication/signup":
                    this.signupPatient(stringMessage);
                    break;
                case "dentist/authentication/signup":
                    this.signupDentist(stringMessage);
                    break;
                case "patient/authentication/login":
                    this.loginPatient(stringMessage);
                    break;    
                case "dentist/authentication/login":
                    this.loginDentist(stringMessage);
                    break;
                case "authentication/dentist/getDentistNamesAlert":
                    List<String> dentistList = objectMapper.readValue(stringMessage, List.class);
                    System.out.println("List of dentist names: " + dentistList);
                    this.publishDentistNames(dentistList);
                    break;
                default:
                    System.err.println("Unrecognized topic: " + topic);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logic to sign up a patient. It reads the value as a PatientSchema and checks for duplicate in the 
     * database, if no duplicates creates a patient otherwise sends an errorMessage 
     * 
     * @param stringPayload the payload that is converted to a String
     * @throws Exception prints the Error Stack trace
     */
    public void signupPatient(String stringPayload){
        ObjectMapper objectMap = new ObjectMapper();

        try {
            System.out.println("Message recieved: " + stringPayload);
            PatientSchema patient = objectMap.readValue(stringPayload, PatientSchema.class);
            
            if (checkPatientInfo(patient)) {

                if(patientService.checkDuplicatePatient(patient)){
                    String errorMessage = "Error: An account with this email already exists";
                    System.out.println(errorMessage);
                    middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 2, false);
                    System.out.println("has published");
                    
                }else{
                    System.out.println(!patientService.checkDuplicatePatient(patient));
                    patientService.createPatient(patient); 
                }            
            } else {
                System.err.println("Some part of the payload for the patient is missing");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     /**
     * Checks if the recieved patientInfo has values in the corresponding attributes.
     * 
     * @param clinicInfo the patientInfo 
     * @return returns true if the patientInfo has all the neccessary information
     */
    public boolean checkPatientInfo(PatientSchema patientInfo) {
        boolean patientInfoExists = true;
        if (patientInfo.getId().isEmpty()) {
            System.err.print("Patient id was not found in the payload");
            patientInfoExists = false;
        } else if (patientInfo.getEmail().isEmpty()) {
            System.err.print("Patient email was not found in the payload");
            patientInfoExists = false;
        } else if (patientInfo.getName().isEmpty()) {
            System.err.print("Patient email was not found in the payload");
            patientInfoExists = false;
        } else if (patientInfo.getPassword().isEmpty()) {
            System.err.println("Patient password was not found in the payload");
            patientInfoExists = false;
        }
        return patientInfoExists;
    }

    /**
     * Logic to sign up a patient. It reads the value as a DemtistSchema and checks for duplicate in the 
     * database, if no duplicates creates a dentist otherwise sends an errorMessage
     * 
     * @param stringPayload the payload that is converted to a String
     * @throws Exception prints the Error Stack trace
     */
    public void signupDentist(String stringPayload){

        try {
            System.out.println("Message recieved: " + stringPayload);
            DentistSchema dentist = objectMapper.readValue(stringPayload, DentistSchema.class);

            if (!checkDentistInfo(dentist)) {
            } else {
                System.err.println("Some part of the payload for the dentist is missing");
                return;
            }

            if(dentistService.checkDuplicateDentist(dentist)){
                String errorMessage = "Error: An account with this email already exists";
                System.out.println(errorMessage);
                middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 2, false);
            }else{

                dentistService.createDentist(dentist);
                String messageToClinicService = String.format("{ \"clinicId\": " + "\"%s\"" +","+"\"dentistId\": "+ "\"%s\"" +" }", dentist.getClinic(), dentist.getId());
                System.out.println("Publishing message to clinic service: " + messageToClinicService);
                middleware.publish(PUBLISHED_CLINIC_TOPIC, messageToClinicService.getBytes(), 2,false);
            }
        } catch (Exception e) {
            System.err.println("An error occurred during dentist singup");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the recieved dentistInfo has values in the corresponding attributes.
     * 
     * @param clinicInfo the dentistInfo 
     * @return returns true if the dentistInfo has all the neccessary information
     */
    public boolean checkDentistInfo(DentistSchema dentistInfo) {
        boolean dentistInfoExists = true;
        if (dentistInfo.getId().isEmpty()) {
            System.err.print("Patient id was not found in the payload");
            dentistInfoExists = false;
        } else if (dentistInfo.getEmail().isEmpty()) {
            System.err.print("Patient email was not found in the payload");
            dentistInfoExists = false;
        } else if (dentistInfo.getName().isEmpty()) {
            System.err.print("Patient email was not found in the payload");
            dentistInfoExists = false;
        } else if (dentistInfo.getPassword().isEmpty()) {
            System.err.println("Patient password was not found in the payload");
            dentistInfoExists = false;
        } else if (dentistInfo.getClinic().isEmpty()) {
            System.err.println("Patient password was not found in the payload");
            dentistInfoExists = false;
        }
        return dentistInfoExists;
    }

    /**
     * Logic to publish Dentist Names to the Clinic service
     * we recieve a list of dentist ids and check for their existence in the database
     * if they exist then we create a JSON for every ID and include its name
     * then we send it to the clinic service, 
     * so that they can display the dentist names in the clinic map
     * 
     * @param dentistList recieve an array of dentistList
     * @throws Exception prints the Error Stack trace
     */
    public void publishDentistNames(List<String> dentistList){
        System.out.println("This is the input dentist list: " + dentistList);
        try {
            List<Map<String, String>> payLoadList = new ArrayList<>();
            for (String dentistID : dentistList ){
                String dentistName = dentistService.getNameByID(dentistID);
                
                Map<String, String> payLoadPart = new HashMap<>();
                payLoadPart.put("id", dentistID);
                payLoadPart.put("name", dentistName);

                payLoadList.add(payLoadPart);
            }
            String finalPayload = objectMapper.writeValueAsString(payLoadList);
            System.out.println("This is the final payload: " + finalPayload);

            middleware.publish(PUBLISHED_DENTIST_TOPIC, finalPayload.getBytes(), 2, false);

        }catch(Exception e){
            System.err.println("Error ocurred whilst trying to publish dentist names:");
            e.printStackTrace();
        }
    }


    /**
     * Logic to login the patient by reading the value as Patient Schema
     * comparing Patients details with the one in database and if its valid
     * publish successMessage User is logged in
     * else publish failure message Invalid Email or password.
     * 
     * @param stringPayload payload of the users login credentials.
     * @throws Exception prints the Error Stack trace
     */
    public void loginPatient(String stringPayload){
        try {

            PatientSchema patient = objectMapper.readValue(stringPayload, PatientSchema.class);
            PatientSchema checkPatient = patientService.getPatient(patient);

            if (!patientService.checkDuplicatePatient(patient) && !patient.checkPassword(checkPatient.getPassword()) ){ 
                String failureMessage = "Invalid email or Password please try again";
                System.out.println(failureMessage);
                middleware.publish(PUBLISHED_LOGIN_TOPIC, failureMessage.getBytes(), 2, false);
            } else {
                String successMessage = "User is sucessfully logged in!";
                String id = checkPatient.getId();

                System.out.println(successMessage);
                middleware.publish(PUBLISHED_LOGIN_TOPIC, successMessage.getBytes(), 2, false);
                middleware.publish(PUBLISHED_USER_ID_TOPIC, id.getBytes(), 2, false);
                System.out.println("Published the Patient ID " + id+ "to topic: " + PUBLISHED_USER_ID_TOPIC);
            }
        } catch (Exception e){
            System.err.println("Error ocurred whilst processing login:");
            e.printStackTrace();
        }
    }

    /**
     * Logic to login the dentist by reading the value as Dentist Schema
     * comparing dentist details with the one in database and if its valid
     * publish successMessage User is logged in
     * else publish failure message Invalid Email or password.
     * 
     * @param stringPayload string containing dentist's credentials
     * @throws Exception prints the Error Stack trace
     */
    public void loginDentist(String stringPayload){
        try{
            
        DentistSchema dentist = objectMapper.readValue(stringPayload, DentistSchema.class);
        DentistSchema checkDentist = dentistService.getDentist(dentist);

        if (!dentistService.checkDuplicateDentist(dentist) && !dentist.checkPassword(checkDentist.getPassword())){ 
            String failureMessage = "Invalid email or Password please try again";
            System.out.println(failureMessage);
            middleware.publish(PUBLISHED_LOGIN_TOPIC, failureMessage.getBytes(), 2, false);
        } else {
            String successMessage = "User is sucessfully logged in!";
            String id = checkDentist.getId();

            System.out.println(successMessage);
            middleware.publish(PUBLISHED_LOGIN_TOPIC, successMessage.getBytes(), 2, false);
            middleware.publish(PUBLISHED_USER_ID_TOPIC, id.getBytes(), 2, false);
            System.out.println("Published the Dentist ID " + id+ " to topic: " + PUBLISHED_USER_ID_TOPIC);
        }
        } catch(Exception e){
            System.err.println("Error ocurred whilst processing login:");
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
