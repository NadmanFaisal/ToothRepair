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
import java.time.LocalDateTime;



import main.java.db.LogSchema;
import main.java.email.EmailService;
import main.java.service.LogService;

@Component
public class MQTT implements MqttCallback {
    private static final String [] BROKER_URLS = { "tcp://test.mosquitto.org", "tcp://broker.hivemq.com", "tcp://broker.emqx.io"};
    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "LogAndNotificationServiceClient";      // Unique client ID
    private static final String[] SUBSCRIBED_TOPICS = {"authenticationService/dentist&patient/userID", "logout"};
    private final LogService logService;
    private final EmailService emailService;
    private LogSchema log;
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private IMqttClient middleware; // MQTT client
    private int currentBrokerIndex = 0;
    
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

            if(topic.equals(SUBSCRIBED_TOPICS[0])){
                String userID = stringMessage;
                this.log.setUserId(userID);
                System.out.println(currentTime +" "+userID+ " Has logged into the Teeth Repair System");
                this.log.setUserLog(currentTime +" "+userID+ " Has logged into the Teeth Repair System");
                this.emailService.sendSimpleMessage("Vaibhavpuram05@gmail.com", "Please work", "Test message");

            }else if(topic.equals(SUBSCRIBED_TOPICS[1])){
                System.out.println("Logged this into the DB: "+currentTime+" "+this.log.getUserId()+" "+stringMessage);
                this.log.setUserLog(currentTime+" "+this.log.getUserId()+" "+stringMessage);
                logService.createLog(log); 
                this.log = new LogSchema();
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
