package main.java.mqtt;

import java.util.ArrayList;
import java.util.List;
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
    private static final String PUBLISHED_CLINIC_TOPIC = "dentist/clinicService/addDentist";
    private static final String PUBLISHED_LOGIN_TOPIC = "authentication/alert/login";
    private static final String PUBLISHED_DENTIST_TOPIC = "authentication/dentist/getDentistNames";
    private static final String PUBLISHED_USER_ID_TOPIC = "authentication/userID";
    private final PatientService patientService; // CRUD Operations for the patient database
    private final DentistService dentistService; // CRUD Operations for the dentist  database
    private static final String[] SUBSCRIBED_TOPICS = { "test/patientAlert", "patient/authentication/signup", "dentist/authentication/signup", "patient/authentication/login", "dentist/authetication/login", "authentication/dentist/getDentistNamesAlert"};
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
            this.threadPool = Executors.newCachedThreadPool(); // Dynamically expand thread poo
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
                            middleware.subscribe(topic, 1); //Subscribe to topic
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
     * Publishing happening with QoS 2.
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
            middleware.publish(PUBLISHED_PATIENT_TOPIC, patientListJson.getBytes(), 2, false);
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
        while (!middleware.isConnected()) {
            try {
                System.out.println("Attempting to reconnect...");
                middleware.reconnect();
                middleware.setCallback(this); // Ensure callback is re-applied
                this.subscribeToTopics();
            } catch (Exception e) {
                System.err.println("Reconnection failed. Retrying...");
                cause.printStackTrace();
            }
        }
        System.out.println("Successfully Reconnected to the Broker");
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
                this.signupPatient(stringMessage);
            } else if(topic.equals(SUBSCRIBED_TOPICS[2])){
                this.signupDentist(stringMessage);               
            } else if(topic.equals(SUBSCRIBED_TOPICS[3])){
                this.loginPatient(stringMessage);
            } else if(topic.equals(SUBSCRIBED_TOPICS[4])){
                this.loginDentist(stringMessage);
            }else if(topic.equals(SUBSCRIBED_TOPICS[5])){
                ArrayList dentistList = objectMap.readValue(stringMessage, ArrayList.class);
                System.out.println("This fixes our problem" + objectMap.readValue(stringMessage, List.class));
                this.publishDentistNames(dentistList);
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
            if(!patientService.checkDuplicatePatient(patient)){
                System.out.println(!patientService.checkDuplicatePatient(patient));
                patientService.createPatient(patient);
                
            }else{
                String errorMessage = "Error: An account with this email already exists";
                System.out.println(errorMessage);
                middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 2, false);
                System.out.println("has published");
                
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Logic to sign up a patient. It reads the value as a DemtistSchema and checks for duplicate in the 
     * database, if no duplicates creates a dentist otherwise sends an errorMessage
     * 
     * @param stringPayload the payload that is converted to a String
     * @throws Exception prints the Error Stack trace
     */
    public void signupDentist(String stringPayload){
        ObjectMapper objectMap = new ObjectMapper();

        try {
            System.out.println("Message recieved: " + stringPayload);
            DentistSchema dentist = objectMap.readValue(stringPayload, DentistSchema.class);
            if(!dentistService.checkDuplicateDentist(dentist)){
                dentistService.createDentist(dentist);
                String messageToClinicService = "{ \"clinicId\": " + "\""+dentist.getClinic()+"\"" +","+"\"dentistId\": "+ "\""+dentist.getId()+"\""+" }";
                System.out.println(messageToClinicService);
                middleware.publish(PUBLISHED_CLINIC_TOPIC, messageToClinicService.getBytes(), 2,false);
                
            }else{
                String errorMessage = "Error: An account with this email already exists";
                System.out.println(errorMessage);
                middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 2, false);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void publishDentistNames(ArrayList dentistList){
        System.out.println(dentistList);
        try{
            String bigBoiPayload = "";
        for( Object dentistID : dentistList ){
            String id = (String) dentistID;
            String dentistName = dentistService.getNameByID(id);
            id = "{ \"id\": " + "\""+ id +"\"" + ", "+" \"name\": "+ "\""+ dentistName+ "\"" +" }, ";
            System.out.println(id);
            bigBoiPayload = bigBoiPayload + id;
            
        }
            bigBoiPayload = bigBoiPayload.substring(0, bigBoiPayload.length() - 2);
            System.out.println(bigBoiPayload);
            middleware.publish(PUBLISHED_DENTIST_TOPIC, bigBoiPayload.getBytes(), 2, false);

        
        }catch(Exception e){
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
        ObjectMapper objectMap = new ObjectMapper();
        try{
            
        PatientSchema patient = objectMap.readValue(stringPayload, PatientSchema.class);
        PatientSchema checkPatient = patientService.getPatient(patient);


        if(patientService.checkDuplicatePatient(patient) && patient.checkPassword(checkPatient.getPassword()) ){ 
            String successMessage = "User is sucessfully logged in!";
            String id = checkPatient.getId();
            System.out.println(successMessage);
            System.out.println(checkPatient.toString());
            System.out.println(id);
            middleware.publish(PUBLISHED_LOGIN_TOPIC, successMessage.getBytes(), 2, false);
            middleware.publish(PUBLISHED_USER_ID_TOPIC, id.getBytes(), 2, false);
            System.out.println("Published the Patient ID " + id+ "to topic: " + PUBLISHED_USER_ID_TOPIC);
        }else{
            String failureMessage = "Invalid email or Password please try again";
            System.out.println(failureMessage);
            middleware.publish(PUBLISHED_LOGIN_TOPIC, failureMessage.getBytes(), 2, false);
        }
        }catch(Exception e){
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
        ObjectMapper objectMap = new ObjectMapper();
        try{
            
        DentistSchema dentist = objectMap.readValue(stringPayload, DentistSchema.class);
        DentistSchema checkDentist = dentistService.getDentist(dentist);

        if(dentistService.checkDuplicateDentist(dentist) && dentist.checkPassword(checkDentist.getPassword())){ 
            String successMessage = "User is sucessfully logged in!";
            String id = checkDentist.getId();
            System.out.println(successMessage);
            System.out.println(checkDentist.toString());
            System.out.println(id);
            middleware.publish(PUBLISHED_LOGIN_TOPIC, successMessage.getBytes(), 2, false);
            middleware.publish(PUBLISHED_USER_ID_TOPIC, id.getBytes(), 2, false);
            System.out.println("Published the Dentist ID " + id+ " to topic: " + PUBLISHED_USER_ID_TOPIC);


        }else{
            String failureMessage = "Invalid email or Password please try again";
            System.out.println(failureMessage);
            middleware.publish(PUBLISHED_LOGIN_TOPIC, failureMessage.getBytes(), 2, false);
        }
        }catch(Exception e){
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
