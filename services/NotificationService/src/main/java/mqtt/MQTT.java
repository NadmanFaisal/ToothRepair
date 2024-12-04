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

import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.db.LogSchema;
import main.java.email.EmailService;
import main.java.service.LogService;

@Component
public class MQTT implements MqttCallback {
    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "LogAndNotificationServiceClient";      // Unique client ID
    private static final String[] SUBSCRIBED_TOPICS = {"authentication/userID", "logout"};
    private final LogService logService;
    private final EmailService emailService;
    private LogSchema log;
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
    public MQTT(LogService logService, EmailService emailService){
        try {
            this.threadPool = Executors.newCachedThreadPool(); // Dynamically expand thread poo
            middleware = new MqttClient(BROKER_URL, CLIENT_ID);
            this.logService = logService;
            this.log = new LogSchema();
            this.emailService = emailService;
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
