package main.java.mqtt;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
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

import main.java.email.EmailService;

@Component
public class MQTT implements MqttCallback {
    private static final String [] BROKER_URLS = { "tcp://broker.hivemq.com", "tcp://broker.emqx.io", "tcp://test.mosquitto.org"};    
    private static final String CLIENT_ID = "LogAndNotificationServiceClient" + UUID.randomUUID().toString();
    private static final String PUBLISHED_TOTAL_MSG_RECEIVED = "notificationService/totalMsgReceived";
    private static final String PUBLISHED_TOTAL_MSG_SENT = "notificationService/totalMsgSent";
    private static final String[] SUBSCRIBED_TOPICS = {"$share/notificationReplica/authenticationService/dentist&patient/userID", "$share/notificationReplica/logout", 
    "$share/notificationReplica/authenticationService/appointment/getAppointmentInfo", "$share/notificationReplica/authenticationService/appointment&patient/getCancelledAppointmentInfo", 
    "$share/notificationReplica/authenticationService/appointment&dentist/getCancelledAppointmentInfo", "$share/notificationReplica/authenticationService/appointment&dentist/getAvailableAppointmentInfo", 
    "$share/notificationReplica/notificationService/totalMsgReceivedAlert", "$share/notificationReplica/notificationService/totalMsgSentAlert" };
    private final EmailService emailService;
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private IMqttClient middleware; // MQTT client
    private int currentBrokerIndex = 0;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String [] patientDetails = {"", ""};
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
    public MQTT(EmailService emailService){
        
            this.threadPool = Executors.newCachedThreadPool(); // Dynamically expand thread poo
            this.emailService = emailService;
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
                        }else{
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
        LocalDateTime currentTime = LocalDateTime.now();
        totalMsgReceived++;

        try {
            String stringMessage = new String(message.getPayload());
            switch (topic) {
                case "authenticationService/appointment/getAppointmentInfo":
                    this.sendGmailNotification(stringMessage, "authenticationService/appointment/getAppointmentInfo");
                    break;
                case "authenticationService/appointment&patient/getCancelledAppointmentInfo":
                    this.sendGmailNotification(stringMessage, "authenticationService/appointment&patient/getCancelledAppointmentInfo");
                    break;
                case "authenticationService/appointment&dentist/getCancelledAppointmentInfo":
                    this.sendGmailNotification(stringMessage, "authenticationService/appointment&dentist/getCancelledAppointmentInfo");
                    break;
                case "authenticationService/appointment&dentist/getAvailableAppointmentInfo":
                    this.sendGmailNotification(stringMessage, "authenticationService/appointment&dentist/getAvailableAppointmentInfo");
                    break;
                case "notificationService/totalMsgReceivedAlert": 
                    System.out.println("PUBLISHING TOTAL MESSAGES RECEIVED: " + this.totalMsgReceived);
                    String msgReceivedString = String.valueOf(totalMsgReceived);
                    middleware.publish(PUBLISHED_TOTAL_MSG_RECEIVED, msgReceivedString.getBytes() , 2, false);
                    totalMsgSent++;
                    break;
                case "notificationService/totalMsgSentAlert": 
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


    public void sendGmailNotification(String stringMessage, String topic){
        System.out.println("RECIEVED UPDATED APPOINTMENT INFO FROM AUTHENTICATIONSERVICE: " + stringMessage);
        try {
            Map<String, Object> appointmentInfo = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});  
            
            String dentistName = (String) appointmentInfo.get("dentistName");
            String patientName = "";
            if((String) appointmentInfo.get("patientName") != null){
                patientName = (String) appointmentInfo.get("patientName");
            }else{
                patientName = patientDetails[0];
            }
            String patientEmail = "";
            if((String) appointmentInfo.get("patientEmail") != null){
                patientEmail = (String) appointmentInfo.get("patientEmail");
            }else{
                patientEmail = patientDetails[1];
            }        
            String appointmentDate = (String) appointmentInfo.get("date");
            String startTime = (String) appointmentInfo.get("startTime");
            String dentistEmail = (String) appointmentInfo.get("dentistEmail");
            String emailBody = "";

            switch (topic) {
                case "authenticationService/appointment/getAppointmentInfo":
                    emailBody = String.format("Hi, %s!\nYour booking with Dr.%s at: %s on the date: %s on TeethRepair has been registered!", patientName, dentistName, startTime, appointmentDate);
                    System.out.println("This is the emailBody: " + emailBody);
                    emailService.sendSimpleMessage(patientEmail, "Successfull Booking of Dentist Appointment!", emailBody);
                    this.patientDetails[0] = patientName;
                    this.patientDetails[1] = patientEmail;
                    break;
                case "authenticationService/appointment&patient/getCancelledAppointmentInfo":
                    emailBody = String.format("Hi, %s!\nYour booking with Patient: %s at: %s on the date: %s on TeethRepair has been cancelled", dentistName, patientName, startTime, appointmentDate);
                    System.out.println("This is the emailBody: " + emailBody);
                    emailService.sendSimpleMessage(dentistEmail , "Your appointment has been cancelled", emailBody);
                    break;
                case "authenticationService/appointment&dentist/getCancelledAppointmentInfo":
                    System.out.println("DENTIST IS CANCELLING APPOINTMENT WITH PATIENT");
                    emailBody = String.format("Hi, %s!\nYour booking with Dr.%s at: %s on the date: %s on TeethRepair has been cancelled", patientName, dentistName, startTime, appointmentDate);
                    System.out.println("This is the emailBody: " + emailBody);
                    emailService.sendSimpleMessage(patientEmail, "Your appointment has been cancelled", emailBody);
                    this.patientDetails[0] = patientName;
                    this.patientDetails[1] = patientEmail;
                    break;
                case "authenticationService/appointment&dentist/getAvailableAppointmentInfo":
                    if(!patientEmail.equals("") && !patientEmail.equals(null)){
                    emailBody = String.format("Hi, %s!\nAn new appointment time slot with with Dr.%s at: %s on the date: %s on TeethRepair is now available to book!", patientName, dentistName, startTime, appointmentDate);
                    System.out.println("This is the emailBody: " + emailBody);
                    this.emailService.sendSimpleMessage(patientEmail, "A new Appointment with Dr." + dentistName +" is now Available!", emailBody);
                    }
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR OCCURED WHEN TRYING TO MAP APPOINTMENT INFO");
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
