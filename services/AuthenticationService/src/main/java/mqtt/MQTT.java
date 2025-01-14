package main.java.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.db.DentistSchema;
import main.java.db.PatientSchema;
import main.java.service.DentistService;
import main.java.service.PatientService;

@Component
public class MQTT implements MqttCallback {
    private static final String [] BROKER_URLS = { "ssl://193a0f31e34647d9a74f1e130a9238ba.s1.eu.hivemq.cloud", "tcp://broker.hivemq.com", "tcp://broker.emqx.io", "tcp://test.mosquitto.org"};
    private static final String CLIENT_ID = "AuthenticationServiceClient" + UUID.randomUUID().toString();
    private static final String PUBLISHED_STATUS_TOPIC = "authenticationService/dentist&patient/status";
    private static final String PUBLISHED_CLINIC_TOPIC = "clinicService/dentist/addDentist";
    private static final String PUBLISHED_LOGIN_TOPIC = "authenticationService/alert/login";
    private static final String PUBLISHED_DENTIST_TOPIC = "authenticationService/dentist/getDentistNames";
    private static final String PUBLISHED_USER_ID_TOPIC = "authenticationService/dentist&patient/userID";
    private static final String PUBLISHED_CLINIC_ID_TOPIC = "Client/AuthenticationService/ClinicId";
    private static final String PUBLISHED_USER_COUNT = "authenticationService/dentist&patient/userCount";
    private static final String PUBLISHED_TOTAL_MSG_SENT = "authenticationService/totalMsgSent";
    private static final String PUBLISHED_TOTAL_MSG_RECEIVED = "authenticationService/totalMsgReceived";
    private static final String PUBLISHED_EMAIL_INFO = "authenticationService/appointment/getAppointmentInfo";
    private static final String PUBLISHED_PATIENT_CANCELLED_APPOINTMENT = "authenticationService/appointment&patient/getCancelledAppointmentInfo";
    private static final String PUBLISHED_DENTIST_CANCELLED_APPOINTMENT = "authenticationService/appointment&dentist/getCancelledAppointmentInfo";
    private static final String PUBLISHED_DENTIST_APPOINTMENT_AVAILABLE = "authenticationService/appointment&dentist/getAvailableAppointmentInfo";
    private static final String PUBLISHED_PATIENT_NAME = "authenticationService/patient/patientName";
    private static final String PUBLISHED_DENTIST_NAME = "authenticationService/dentist/dentistName";

    private final PatientService patientService; // CRUD Operations for the patient database
    private final DentistService dentistService; // CRUD Operations for the dentist  database
    private static final String[] SUBSCRIBED_TOPICS = { "$share/authenticationReplica/authenticationService/patient/signup", "$share/authenticationReplica/authenticationService/dentist/signup", 
    "$share/authenticationReplica/authenticationService/patient/login", "$share/authenticationReplica/authenticationService/dentist/login", 
    "$share/authenticationReplica/authenticationService/dentist/getDentistNamesAlert", "$share/authenticationReplica/AuthenticationService/Dentist/GetClinicId", 
    "$share/authenticationReplica/authenticationService/patient/logout" ,"$share/authenticationReplica/authenticationService/dentist/logout", 
    "$share/authenticationReplica/authenticationService/users/getActiveUsersAlert", "$share/authenticationReplica/authenticationService/totalMsgSentAlert", "$share/authenticationReplica/authenticationService/totalMsgReceivedAlert",
    "$share/authenticationReplica/scheduleService/dentist&patient/sendBookingIdToAuth", "$share/authenticationReplica/scheduleService/patient/sendCancellingIdToAuth", "$share/authenticationReplica/scheduleService/dentist/sendCancellingIdToAuth", "$share/authenticationReplica/scheduleService/dentist/sendAvailableIdToAuth", "$share/authenticationReplica/authenticationService/patient/getPatientName", "$share/authenticationReplica/authenticationService/dentist/getDentistName" };
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private IMqttClient middleware; // MQTT client
    private ObjectMapper objectMapper = new ObjectMapper();
    private MqttConnectOptions options = new MqttConnectOptions();
    private int currentBrokerIndex = 0;
    private boolean STRESS_TEST_MODE = false;
    private int userCount = 0;
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
    public MQTT(PatientService patientService, DentistService dentistService){
        this.threadPool = Executors.newCachedThreadPool(); // Dynamically expand thread pool
        this.patientService = patientService;
        this.dentistService = dentistService;
        
        if(STRESS_TEST_MODE){
        options.setUserName("Administrator");
        String passwordString = "Vaibhav12Taha";
        char[] passwordChars = passwordString.toCharArray();
        options.setPassword(passwordChars);
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
                middleware = new MqttClient(BROKER_URLS[i], CLIENT_ID);
                if(STRESS_TEST_MODE && BROKER_URLS[i].equals("ssl://193a0f31e34647d9a74f1e130a9238ba.s1.eu.hivemq.cloud")){
                    middleware.connect(options);
                } else {
                    middleware.connect();
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
                    if(BROKER_URLS[currentBrokerIndex].equals("ssl://193a0f31e34647d9a74f1e130a9238ba.s1.eu.hivemq.cloud")){
                        middleware.connect(options);
                    } else {
                        middleware.connect();
                    }
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
    public void publishActiveUserCount(){
        try {
            this.userCount = patientService.getActivePatients() + dentistService.getActiveDentists();
            String userCountString = String.valueOf(this.userCount);
            this.middleware.publish(PUBLISHED_USER_COUNT, userCountString.getBytes(), 2, false);
            totalMsgSent++;
            System.out.println("THIS IS THE CURRENT USER COUNT OF THE SYSTEM: "+ userCountString);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FAILED TO SEND ACTIVE USER COUNT");
        }
    }
    
     @Override
    public void messageArrived(String topic, MqttMessage message) {
        totalMsgReceived++ ;
        
        try {
            String stringMessage = new String(message.getPayload());
            System.out.println("Message recieved: " + stringMessage + " Topic: " + topic);

            switch (topic) {
                case "authenticationService/patient/signup":
                    this.signupPatient(stringMessage);
                    break;
                case "authenticationService/dentist/signup":
                    this.signupDentist(stringMessage);
                    break;
                case "authenticationService/patient/login":
                    this.loginPatient(stringMessage);
                    this.publishActiveUserCount();
                    break;    
                case "authenticationService/dentist/login":
                    this.loginDentist(stringMessage);
                    this.publishActiveUserCount();
                    break;
                case "authenticationService/dentist/getDentistNamesAlert":
                    List<String> dentistList = objectMapper.readValue(stringMessage, List.class);
                    System.out.println("List of dentist names: " + dentistList);
                    this.publishDentistNames(dentistList);
                    break;
                case "AuthenticationService/Dentist/GetClinicId":
                    this.getClinicId(stringMessage);
                    break;
                case "authenticationService/patient/logout" :
                    System.out.println("USER HAS LOGGED OUT WITH THE ID :" + stringMessage);
                    PatientSchema optionalLoggedOutPatient = patientService.getPatientByID(stringMessage);
                    patientService.setIsLoggedIn(optionalLoggedOutPatient, false);
                    break;
                case "authenticationService/dentist/logout":
                    System.out.println("USER HAS LOGGED OUT WITH THE ID :" + stringMessage);
                    DentistSchema optionalLoggedOutDentist = dentistService.getDentistByID(stringMessage);
                    dentistService.setIsLoggedIn(optionalLoggedOutDentist, false);
                    break;
                case "authenticationService/users/getActiveUsersAlert":
                    this.publishActiveUserCount();
                    System.out.println("SENT ALL ACTIVE USERS");
                    break;
                case "authenticationService/totalMsgReceivedAlert":
                    System.out.println("PUBLISHING TOTAL MESSAGES RECEIVED: " + this.totalMsgReceived);
                    String msgReceivedString = String.valueOf(totalMsgReceived);
                    middleware.publish(PUBLISHED_TOTAL_MSG_RECEIVED, msgReceivedString.getBytes() , 2, false);
                    totalMsgSent++;
                    break;
                case "authenticationService/totalMsgSentAlert":
                    totalMsgSent++;    
                    System.out.println("PUBLISHING TOTAL MESSAGES SENT: " + this.totalMsgSent);
                    String msgSentString = String.valueOf(totalMsgSent);
                    middleware.publish(PUBLISHED_TOTAL_MSG_SENT, msgSentString.getBytes(), 2, false);
                    break;
                case "scheduleService/dentist&patient/sendBookingIdToAuth":
                    System.out.println("MSG RECIEVED FROM SCHEDULESERVICE IN TOPIC: " + topic + "WITH MESSAGE: " + message);
                    Map<String, Object> appointmentInfo = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});
                    String dentistId = (String) appointmentInfo.get("dentist");
                    String patientId = (String) appointmentInfo.get("patient");

                    String patientName = patientService.getNameByID(patientId);
                    String patientEmail = patientService.getEmailById(patientId);
                    String dentistName = dentistService.getNameByID(dentistId);
                    String dentistEmail = dentistService.getEmailById(dentistId);
                    
                    appointmentInfo.put("patientName", patientName);
                    appointmentInfo.put("patientEmail", patientEmail);
                    appointmentInfo.put("dentistName", dentistName);
                    appointmentInfo.put("dentistEmail", dentistEmail);
                    
                    String updatedAppointmentInfo = objectMapper.writeValueAsString(appointmentInfo);
                    middleware.publish(PUBLISHED_EMAIL_INFO, updatedAppointmentInfo.getBytes(), 2, false);
                    System.out.println("PUBLISHED UPDATED APPOINTMENT INFO TO NOTIFICATIONSERVICE: " + updatedAppointmentInfo);
                    break;
                case "scheduleService/patient/sendCancellingIdToAuth":
                    System.out.println("MSG RECIEVED FROM SCHEDULESERVICE IN TOPIC: " + topic + "WITH MESSAGE: " + message);
                    Map<String, Object> appointmentInfo2 = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});
                    String dentistId2 = (String) appointmentInfo2.get("dentist");
                    String patientId2 = (String) appointmentInfo2.get("patient");

                    String patientName2 = patientService.getNameByID(patientId2);
                    String patientEmail2 = patientService.getEmailById(patientId2);
                    String dentistName2 = dentistService.getNameByID(dentistId2);
                    String dentistEmail2 = dentistService.getEmailById(dentistId2);
                    
                    appointmentInfo2.put("patientName", patientName2);
                    appointmentInfo2.put("patientEmail", patientEmail2);
                    appointmentInfo2.put("dentistName", dentistName2);
                    appointmentInfo2.put("dentistEmail", dentistEmail2);
                    
                    String updatedAppointmentInfo2 = objectMapper.writeValueAsString(appointmentInfo2);
                    middleware.publish(PUBLISHED_PATIENT_CANCELLED_APPOINTMENT, updatedAppointmentInfo2.getBytes(), 2, false);
                    System.out.println("PUBLISHED UPDATED APPOINTMENT INFO TO NOTIFICATIONSERVICE: " + updatedAppointmentInfo2);
                    break;
                case "scheduleService/dentist/sendCancellingIdToAuth":
                    System.out.println("MSG RECIEVED FROM SCHEDULESERVICE IN TOPIC: " + topic + "WITH MESSAGE: " + message);
                    Map<String, Object> appointmentInfo3 = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});
                    String dentistId3 = (String) appointmentInfo3.get("dentist");
                    String patientId3 = (String) appointmentInfo3.get("patient");

                    String patientName3 = patientService.getNameByID(patientId3);
                    String patientEmail3 = patientService.getEmailById(patientId3);
                    String dentistName3 = dentistService.getNameByID(dentistId3);
                    String dentistEmail3 = dentistService.getEmailById(dentistId3);
                    
                    appointmentInfo3.put("patientName", patientName3);
                    appointmentInfo3.put("patientEmail", patientEmail3);
                    appointmentInfo3.put("dentistName", dentistName3);
                    appointmentInfo3.put("dentistEmail", dentistEmail3);
                    
                    String updatedAppointmentInfo3 = objectMapper.writeValueAsString(appointmentInfo3);
                    middleware.publish(PUBLISHED_DENTIST_CANCELLED_APPOINTMENT, updatedAppointmentInfo3.getBytes(), 2, false);
                    System.out.println("PUBLISHED UPDATED APPOINTMENT INFO TO NOTIFICATIONSERVICE: " + updatedAppointmentInfo3);
                    break;
                case "scheduleService/dentist/sendAvailableIdToAuth":
                    System.out.println("MSG RECIEVED FROM SCHEDULESERVICE IN TOPIC: " + topic + "WITH MESSAGE: " + message);
                    Map<String, Object> appointmentInfo4 = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});
                    String dentistId4 = (String) appointmentInfo4.get("dentist");

                    String dentistName4 = dentistService.getNameByID(dentistId4);
                    String dentistEmail4 = dentistService.getEmailById(dentistId4);
                    
                    appointmentInfo4.put("dentistName", dentistName4);
                    appointmentInfo4.put("dentistEmail", dentistEmail4);
                    
                    String updatedAppointmentInfo4 = objectMapper.writeValueAsString(appointmentInfo4);
                    middleware.publish(PUBLISHED_DENTIST_APPOINTMENT_AVAILABLE, updatedAppointmentInfo4.getBytes(), 2, false);
                    System.out.println("PUBLISHED UPDATED APPOINTMENT INFO TO NOTIFICATIONSERVICE: " + updatedAppointmentInfo4);
                    break;
                case "authenticationService/patient/getPatientName":
                    this.publishPatientName(stringMessage);
                    break;
                case "authenticationService/dentist/getDentistName":
                    this.publishDentistName(stringMessage);
                    break;
                default:
                    System.err.println("Unrecognized topic: " + topic);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishDentistName(String dentistId) {
        try {
            System.out.println(dentistId);
            String dentistName = dentistService.getNameByID(dentistId);
            if (dentistName != null) {
                middleware.publish(PUBLISHED_DENTIST_NAME, dentistName.getBytes(), 2, false);
                totalMsgSent++;
                System.out.println("Published dentist name: " + dentistName + " for ID: " + dentistId);
            } else {
                String errorMessage = "Dentist not found for ID: " + dentistId;
                middleware.publish(PUBLISHED_DENTIST_NAME, errorMessage.getBytes(), 2, false);
                totalMsgSent++;
                System.err.println(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to publish dentist name for ID: " + dentistId);
        }
    }

    private void publishPatientName(String patientId) {
        try {
            System.out.println(patientId);
            String patientName = patientService.getNameByID(patientId);
            if (patientName != null) {
                middleware.publish(PUBLISHED_PATIENT_NAME, patientName.getBytes(), 2, false);
                totalMsgSent++;
                System.out.println("Published patient name: " + patientName + " for ID: " + patientId);
            } else {
                String errorMessage = "Patient not found for ID: " + patientId;
                middleware.publish(PUBLISHED_PATIENT_NAME, errorMessage.getBytes(), 2, false);
                totalMsgSent++;
                System.err.println(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to publish patient name for ID: " + patientId);
        }
    }
    

    public void getClinicId(String stringPayload) {
        try {
            String dentistId = objectMapper.readTree(stringPayload).get("id").asText();
            String clinicId = dentistService.getClinicIdByDentistId(dentistId);
            String responseMessage = objectMapper.writeValueAsString(clinicId);
            middleware.publish(PUBLISHED_CLINIC_ID_TOPIC, responseMessage.getBytes(), 2, false);
            totalMsgSent++;
            System.out.println("Published clinicId: " + clinicId + " to topic: " + PUBLISHED_CLINIC_ID_TOPIC);
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
        try {
            System.out.println("Message recieved: " + stringPayload);
            PatientSchema patient = objectMapper.readValue(stringPayload, PatientSchema.class);
            
            if (checkPatientInfo(patient)) {
                if(patientService.checkDuplicatePatient(patient)){
                    String errorMessage = "Error: An account with this email already exists";
                    System.out.println(errorMessage);
                    middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 2, false);
                    totalMsgSent++;
                    System.out.println("has published");
                    
                }else{
                    System.out.println(!patientService.checkDuplicatePatient(patient));
                    patient.setIsLoggedIn(false);
                    patientService.createPatient(patient); 
                }            
            } else {
                String errorMessage = "Some part of the payload for the patient is missing";
                System.out.println(errorMessage);
                middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 2, false);
                totalMsgSent++;
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
        if (patientInfo.getEmail().isEmpty()) {
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
     * Logic to sign up a dentist. It reads the value as a DentistSchema and checks for duplicate in the 
     * database, if no duplicates creates a dentist otherwise sends an errorMessage
     * 
     * @param stringPayload the payload that is converted to a String
     * @throws Exception prints the Error Stack trace
     */
    public void signupDentist(String stringPayload){

        try {
            System.out.println("Message recieved: " + stringPayload);
            DentistSchema dentist = objectMapper.readValue(stringPayload, DentistSchema.class);

            if (checkDentistInfo(dentist)) {
                if(dentistService.checkDuplicateDentist(dentist)){
                    String errorMessage = "Error: An account with this email already exists";
                    System.out.println(errorMessage);
                    middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 2, false);
                    totalMsgSent++;
                }else{
                    dentist.setIsLoggedIn(false);
                    dentistService.createDentist(dentist);
                    String messageToClinicService = String.format("{ \"clinicId\": " + "\"%s\"" +","+"\"dentistId\": "+ "\"%s\"" +" }", dentist.getClinic(), dentist.getId());
                    System.out.println("Publishing message to clinic service: " + messageToClinicService);
                    middleware.publish(PUBLISHED_CLINIC_TOPIC, messageToClinicService.getBytes(), 2,false);
                    totalMsgSent++;
                }

            } else {
                String errorMessage = "Some part of the payload for the dentist is missing";
                System.out.println(errorMessage);
                middleware.publish(PUBLISHED_STATUS_TOPIC, errorMessage.getBytes(), 2, false);
                totalMsgSent++;
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
        if (dentistInfo.getEmail().isEmpty()) {
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
            totalMsgSent++;

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
    public void loginPatient(String stringPayload) {
        try {
            PatientSchema patient = objectMapper.readValue(stringPayload, PatientSchema.class);

            PatientSchema checkPatient = patientService.getPatient(patient);

            if (checkPatient == null) {
                String failureMessage = "Invalid login information, there is no patient with that email.";
                System.out.println(failureMessage);
                middleware.publish(PUBLISHED_LOGIN_TOPIC, failureMessage.getBytes(), 2, false);
                totalMsgSent++;
                return;
            }
    
            if (!patient.checkPassword(checkPatient.getPassword())) {
                String failureMessage = "Invalid email or Password please try again";
                System.out.println(failureMessage);
                middleware.publish(PUBLISHED_LOGIN_TOPIC, failureMessage.getBytes(), 2, false);
                totalMsgSent++;
                return;
            }
    
            String successMessage = "User is sucessfully logged in!";
            System.out.println(successMessage);
            middleware.publish(PUBLISHED_LOGIN_TOPIC, successMessage.getBytes(), 2, false);
            totalMsgSent++;
    
            patientService.setIsLoggedIn(checkPatient, true);
    
            String userId = checkPatient.getId();
            middleware.publish(PUBLISHED_USER_ID_TOPIC, userId.getBytes(), 2, false);
            totalMsgSent++;
            System.out.println("Published the Patient ID " + userId + " to topic: " + PUBLISHED_USER_ID_TOPIC);
    
        } catch (Exception e) {
            System.err.println("Error occurred whilst processing login:");
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
            if (checkDentist == null) {
                String failureMessage = "Invalid login information, there is no dentist with that information";
                System.out.println(failureMessage);
                middleware.publish(PUBLISHED_LOGIN_TOPIC, failureMessage.getBytes(), 2, false);
                totalMsgSent++;
            } else if (!dentistService.checkDuplicateDentist(dentist) && !dentist.checkPassword(checkDentist.getPassword())){ 
                String failureMessage = "Invalid email or Password please try again";
                System.out.println(failureMessage);
                middleware.publish(PUBLISHED_LOGIN_TOPIC, failureMessage.getBytes(), 2, false);
                totalMsgSent++;
            } else {
                String successMessage = "User is sucessfully logged in!";
                String id = checkDentist.getId();
                dentistService.setIsLoggedIn(checkDentist, true);
                System.out.println(successMessage);
                middleware.publish(PUBLISHED_LOGIN_TOPIC, successMessage.getBytes(), 2, false);
                totalMsgSent++;
                middleware.publish(PUBLISHED_USER_ID_TOPIC, id.getBytes(), 2, false);
                totalMsgSent++;
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