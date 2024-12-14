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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import main.java.db.AppointmentSchema;
import main.java.service.AppointmentService;

@Component
public class MQTT implements MqttCallback {
    private static final String BROKER_URL = "tcp://broker.hivemq.com";
    private static final String CLIENT_ID = "ScheduleClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "Client/ScheduleService/AppointmentInfo";
    private final AppointmentService appointmentService; // CRUD Operations for the schedule database
    private static final String[] SUBSCRIBED_TOPICS = {"ScheduleService/Appointment/getAppointments",
     "ScheduleService/Appointment/createAppointment", "ScheduleService/Appointment/bookAppointment",
     "ScheduleService/Appointment/changeAppointmentStatus", "ScheduleService/Appointment/getAppointmentsByClinic", "ScheduleService/Appointment/getAppointmentsByPatient"}; 
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private final IMqttClient middleware; // MQTT client
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    
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
            this.middleware = new MqttClient(BROKER_URL, CLIENT_ID);
            middleware.connect();
            System.out.println("Service connected to mqtt");
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
         // while client is connected
            for (String topic : SUBSCRIBED_TOPICS) {
                System.out.println("subscribed to: " + topic);
                threadPool.submit(()-> {
                    try {
                        if (middleware.isConnected()){
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
     * Publishes the topic as a String in JSON notation.
     * 
     * Publishing happening with QoS 1.
     * 
     * It publishes with the conected client
     * 
     * @param N/A no params needed
     * @throws MqttException prints the Error Stack trace
     */
    private void publishAppointmentList(String topic, String message){
        try {
            //Publish the payload as bytes to the topic.
            System.out.println(message);
            middleware.publish(PUBLISHED_TOPIC, message.getBytes(), 2, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
/* 
    public void createAppointment(AppointmentSchema appointmentInformation) {
        System.out.println("AppointmentInfo has been saved into the database: " + appointmentInformation);
        this.appointmentService.createAppointment(appointmentInformation);
    }
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
            System.out.println("Topic given: " + topic);
            switch (topic) {
                case "ScheduleService/Appointment/getAppointments": {
                    if(stringMessage.equals("Get Appointments")){
                        System.out.println("Will publish all appointments");
                        String appointmentListJson = objectMapper.writeValueAsString(this.appointmentService.getAllAppointments());
                        this.publishAppointmentList(topic, appointmentListJson);
                    }   
                    break;
                }

                case "ScheduleService/Appointment/getAppointmentsByClinic": {
                    System.out.println("Will publish all appointments per clinic");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    String appointmentListJson = objectMapper.writeValueAsString(this.appointmentService.getAppointmentsByClinic(appointmentInfo));
                    this.publishAppointmentList(topic, appointmentListJson);
                    break;
                }

                case "ScheduleService/Appointment/getAppointmentsByPatient": {
                    System.out.println("Will publish all appointments per patient");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    String appointmentListJson = objectMapper.writeValueAsString(this.appointmentService.getAppointmentsByPatient(appointmentInfo));
                    this.publishAppointmentList(topic, appointmentListJson);
                    break;
                }

                case "ScheduleService/Appointment/createAppointment": {
                    System.out.println("Entered createAppointment if statement");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    System.out.println(appointmentInfo.toString());
                    this.appointmentService.createAppointment(appointmentInfo);
                    break;
                }

                case "ScheduleService/Appointment/bookAppointment": {
                    System.out.println("Entered bookAppointment if statement");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    System.out.println(appointmentInfo.toString());
                    appointmentService.bookAppointment(appointmentInfo);
                    break;
                }

                case "ScheduleService/Appointment/changeAppointmentStatus": {
                    System.out.println("Entered changeAppointmentStatus if statement");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    System.out.println(appointmentInfo.toString());
                    appointmentService.changeAppointmentStatus(appointmentInfo);
                    break;
                }

                default:{
                    System.out.println("The topic is invalid");
                    break;
                }
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
