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

import main.java.db.AppointmentSchema;
import main.java.service.AppointmentService;

@Component
public class MQTT implements MqttCallback {
    private static final String BROKER_URL = "tcp://test.mosquitto.org";
    private static final String CLIENT_ID = "ScheduleClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "test/appointmentList";
    private final AppointmentService appointmentService; // CRUD Operations for the schedule database
    private static final String[] SUBSCRIBED_TOPICS = {"test/appointmentAlert"}; 
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private final IMqttClient middleware; // MQTT client

    /**
     * MQTT class Constructor
     * Initializes the MQTT client, connects to the broker and subscribes to the topics.
     * 
     * @param appointmentService Connection to the database, where it interacts withe the MongoDB database using CRUD operators
     * @throws MqttException throws a RuntimeException showing the error
     */

    @Autowired
    public MQTT(AppointmentService appointmentService){
        try {
            this.threadPool = Executors.newFixedThreadPool(SUBSCRIBED_TOPICS.length);
            this.appointmentService = appointmentService;
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
        while(middleware.isConnected()){ // while client is connected
                threadPool.submit(()-> {
                    try {
                        middleware.subscribe(SUBSCRIBED_TOPIC[0], 0); //Subscribe to topic
                        System.out.println("subscribed to" + topic)
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            try {
                Thread.sleep(1000); // 1 second interval
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
    private void publishAppointmentList(){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String appointmentListJson = objectMapper.writeValueAsString(this.appointmentService.getAllAppointments());
            String emptyMessage = "";
            //Publish the payload as bytes to the topic.
            
            middleware.publish(PUBLISHED_TOPIC, appointmentListJson.getBytes(), 1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

    public void createAppointment(AppointmentSchema appointmentInformation) {
        System.out.println("AppointmentInfo has been saved into the database: " + appointmentInformation);
        this.appointmentService.createAppointment(appointmentInformation);
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
                   
            System.out.println("Message recieved: " + stringMessage);
            if (topic.equals("test/appointmentAlert")) {
                if(stringMessage.equals("Get Appointments")){
                    this.publishAppointmentList();
                }
            } else if (topic.equals("test/createAppointment") ) {
                System.out.println("Entered createAppointment if statement");
                ObjectMapper objectMapper = new ObjectMapper();
                AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                appointmentService.createAppointment(appointmentInfo);
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
