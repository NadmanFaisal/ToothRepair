package main.java.mqtt;

import java.time.LocalDateTime;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.db.LogSchema;
import main.java.email.EmailService;
import main.java.service.LogService;

@Component
public class MQTT implements MqttCallback {
    private static final String [] BROKER_URLS = {"tcp://broker.hivemq.com",  "tcp://test.mosquitto.org", "tcp://broker.emqx.io"};
    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "LogAndNotificationServiceClient";      // Unique client ID
    private static final String[] SUBSCRIBED_TOPICS = {"authenticationService/dentist&patient/userID", "logout", 
    "authenticationService/appointment/getAppointmentInfo", "authenticationService/appointment&patient/getCancelledAppointmentInfo", 
    "authenticationService/appointment&dentist/getCancelledAppointmentInfo", "authenticationService/appointment&dentist/getAvailableAppointmentInfo"};
    private final LogService logService;
    private final EmailService emailService;
    private LogSchema log;
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private IMqttClient middleware; // MQTT client
    private int currentBrokerIndex = 0;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String [] patientDetails = new String[2];

    /**
     * MQTT class Constructor
     * Initializes the MQTT client, connects to the broker and subscribes to the topics.
     * 
     * @param patientSerivce Connection to the database, where it interacts withe the MongoDB database using CRUD operators
     * @throws MqttException throws a RuntimeException showing the error
     */

    @Autowired
    public MQTT(LogService logService, EmailService emailService){
        
            this.threadPool = Executors.newCachedThreadPool(); // Dynamically expand thread poo
            this.logService = logService;
            this.log = new LogSchema();
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
        try {
            String stringMessage = new String(message.getPayload());

            switch (topic) {
                case "authenticationService/dentist&patient/userID":
                    String userID = stringMessage;
                    this.log.setUserId(userID);
                    System.out.println(currentTime +" "+userID+ " Has logged into the Teeth Repair System");
                    this.log.setUserLog(currentTime +" "+userID+ " Has logged into the Teeth Repair System");
                    this.emailService.sendSimpleMessage("Vaibhavpuram05@gmail.com", "Please work", "Test message");
                    break;
                case "logout":    
                    System.out.println("Logged this into the DB: "+currentTime+" "+this.log.getUserId()+" "+stringMessage);
                    this.log.setUserLog(currentTime+" "+this.log.getUserId()+" "+stringMessage);
                    logService.createLog(log); 
                    this.log = new LogSchema();
                    break;
                case "authenticationService/appointment/getAppointmentInfo":
                    System.out.println("RECIEVED UPDATED APPOINTMENT INFO FROM AUTHENTICATIONSERVICE: " + stringMessage);
                    Map<String, Object> appointmentInfo = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});
                    
                    String dentistName = (String) appointmentInfo.get("dentistName");
                    String patientName = (String) appointmentInfo.get("patientName");
                    String patientEmail = (String) appointmentInfo.get("patientEmail");
                    String appointmentDate = (String) appointmentInfo.get("date");
                    String startTime = (String) appointmentInfo.get("startTime");
                    this.patientDetails[0] = patientName;
                    this.patientDetails[1] = patientEmail;
                    String emailBody = String.format("Hi, %s!\nYour booking with Dr.%s at: %s on the date: %s on TeethRepair has been registered!", patientName, dentistName, startTime, appointmentDate);
                    System.out.println("This is the emailBody: " + emailBody);
                    this.emailService.sendSimpleMessage(patientEmail, "Successfull Booking of Dentist Appointment!", emailBody);

                    break;
                case "authenticationService/appointment&patient/getCancelledAppointmentInfo":
                    System.out.println("RECIEVED UPDATED APPOINTMENT INFO FROM AUTHENTICATIONSERVICE: " + stringMessage);
                    Map<String, Object> appointmentInfo2 = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});
                    
                    String dentistName2 = (String) appointmentInfo2.get("dentistName");
                    String patientName2 = (String) appointmentInfo2.get("patientName");
                    String dentistEmail2 = (String) appointmentInfo2.get("dentistEmail");
                    String appointmentDate2 = (String) appointmentInfo2.get("date");
                    String startTime2 = (String) appointmentInfo2.get("startTime");

                    String emailBody2 = String.format("Hi, %s!\nYour booking with Patient: %s at: %s on the date: %s on TeethRepair has been cancelled", dentistName2, patientName2, startTime2, appointmentDate2);
                    System.out.println("This is the emailBody: " + emailBody2);
                    this.emailService.sendSimpleMessage(dentistEmail2 , "Your appointment has been cancelled", emailBody2);
                    break;
                case "authenticationService/appointment&dentist/getCancelledAppointmentInfo":
                    System.out.println("RECIEVED UPDATED APPOINTMENT INFO FROM AUTHENTICATIONSERVICE: " + stringMessage);
                    Map<String, Object> appointmentInfo3 = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});
                    
                    String dentistName3 = (String) appointmentInfo3.get("dentistName");
                    String patientName3 = (String) appointmentInfo3.get("patientName");
                    String patientEmail3 = (String) appointmentInfo3.get("patientEmail");
                    String appointmentDate3 = (String) appointmentInfo3.get("date");
                    String startTime3 = (String) appointmentInfo3.get("startTime");

                    String emailBody3 = String.format("Hi, %s!\nYour booking with Dr.%s at: %s on the date: %s on TeethRepair has been cancelled", patientName3, dentistName3, startTime3, appointmentDate3);
                    System.out.println("This is the emailBody: " + emailBody3);
                    this.emailService.sendSimpleMessage(patientEmail3, "Your appointment has been cancelled", emailBody3);
                    break;
                case "authenticationService/appointment&dentist/getAvailableAppointmentInfo":
                    System.out.println("RECIEVED UPDATED APPOINTMENT INFO FROM AUTHENTICATIONSERVICE: " + stringMessage);
                    Map<String, Object> appointmentInfo4 = objectMapper.readValue(stringMessage, new TypeReference<Map<String, Object>>() {});
                    
                    String dentistName4 = (String) appointmentInfo4.get("dentistName");
                    String appointmentDate4 = (String) appointmentInfo4.get("date");
                    String startTime4 = (String) appointmentInfo4.get("startTime");
                    String patientName4 = this.patientDetails[0];
                    String patientEmail4 = this.patientDetails[1];

                    String emailBody4 = String.format("Hi, %s!\nAn appointment time slot with with Dr.%s at: %s on the date: %s on TeethRepair is now available to book!", patientName4, dentistName4, startTime4, appointmentDate4);
                    System.out.println("This is the emailBody: " + emailBody4);
                    this.emailService.sendSimpleMessage(patientEmail4, "A new Appointment with Dr." + dentistName4 +" is now Available!", emailBody4);
                    break;
                default:
                    break;
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
