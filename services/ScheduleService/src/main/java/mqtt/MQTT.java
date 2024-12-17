package main.java.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import main.java.db.AppointmentSchema;
import main.java.service.AppointmentService;

@Component
public class MQTT implements MqttCallback {
    private static final String [] BROKER_URLS = { "tcp://broker.hivemq.com", "tcp://test.mosquitto.org", "tcp://broker.emqx.io"};
    private static final String CLIENT_ID = "ScheduleClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "Client/ScheduleService/AppointmentInfo";
    private static final String PUBLISHED_AVAILABLE_APPOINTMENT_COUNT_TOPIC = "scheduleService/availableAppointmentCount";
    private static final String PUBLISHED_TOTAL_MSG_SENT = "scheduleService/totalMsgSent";
    private static final String PUBLISHED_TOTAL_MSG_RECEIVED = "scheduleService/totalMsgReceived";
    private static final String PUBLISHED_ENTITY_IDS = "scheduleService/dentist&patient/sendBookingIdToAuth";
    private static final String PUBLISHED_ENTITY_IDS_CANCEL = "scheduleService/dentist&patient/sendCancellingIdToAuth";
    private final AppointmentService appointmentService; // CRUD Operations for the schedule database
    private static final String[] SUBSCRIBED_TOPICS = {"ScheduleService/Appointment/getAppointments",
     "ScheduleService/Appointment/createAppointment", "ScheduleService/Appointment/bookAppointment",
     "ScheduleService/Appointment/makeAppointmentAvailable", "ScheduleService/Appointment/getAppointmentsByClinic",
     "ScheduleService/Appointment/getAppointmentsByPatient", "ScheduleService/Appointment/dentistCancelAppointments",
    "ScheduleService/Appointment/patientCancelAppointments", "ScheduleService/Appointment/getAppointmentsByDentist", "scheduleService/appointment/getAvailableAppointmentsAlert", "scheduleService/totalMsgSentAlert", "scheduleService/totalMsgReceivedAlert"}; 
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private IMqttClient middleware; // MQTT client
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private int currentBrokerIndex = 0;
    private int totalMsgReceived = 0;
    private int totalMsgSent = 0;


    
    /**
     * MQTT class Constructor
     * Initializes the MQTT client, connects to the broker and subscribes to the topics.
     * 
     * @param appointmentService Connection to the database, where it interacts withe the MongoDB database using CRUD operators
     * @throws MqttException throws a RuntimeException showing the error
     */

    @Autowired
    public MQTT(AppointmentService appointmentService){
        this.threadPool = Executors.newFixedThreadPool(SUBSCRIBED_TOPICS.length);
        this.appointmentService = appointmentService;
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
         // while client is connected
            for (String topic : SUBSCRIBED_TOPICS) {
                threadPool.submit(()-> {
                    try {
                        if (middleware.isConnected()){
                            middleware.subscribe(topic, 1); //Subscribe to topic
                            System.out.println("ScheduleService subscribed to topic: " + topic);
                        } else {
                            System.out.println("ScheduleService is not connected to the broker. Cannot subscribe to topic: " + topic);
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
            totalMsgSent++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
/* 
    public void createAppointment(AppointmentSchema appointmentInformation) {
        System.out.println("AppointmentInfo has been saved into the database: " + appointmentInformation);
        this.appointmentService.createAppointment(appointmentInformation);
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
        totalMsgReceived++;
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

                case "ScheduleService/Appointment/getAppointmentsByDentist": {
                    System.out.println("Will publish all appointments per dentist");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    String appointmentListJson = objectMapper.writeValueAsString(this.appointmentService.getAppointmentsByDentist(appointmentInfo));
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
                    middleware.publish(PUBLISHED_ENTITY_IDS, this.publishEntityIds(appointmentInfo.getId()).getBytes(), 2, false);
                    break;
                }
                case "ScheduleService/Appointment/makeAppointmentAvailable": {
                    System.out.println("Entered makeAppointmentAvailable if statement");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    System.out.println(appointmentInfo.toString());
                    appointmentService.makeAppointmentAvailable(appointmentInfo);
                    break;
                }

                case "ScheduleService/Appointment/dentistCancelAppointments": {
                    System.out.println("Entered dentist cancel if statement");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    System.out.println(appointmentInfo.toString());
                    middleware.publish(PUBLISHED_ENTITY_IDS_CANCEL, this.publishEntityIds(appointmentInfo.getId()).getBytes(), 2, false);
                    appointmentService.dentistCancel(appointmentInfo);
                    break;
                }

                case "ScheduleService/Appointment/patientCancelAppointments": {
                    System.out.println("Entered patient cancel if statement");
                    AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                    System.out.println(appointmentInfo.toString());
                    appointmentService.patientCancel(appointmentInfo);
                    middleware.publish(PUBLISHED_ENTITY_IDS_CANCEL, this.publishEntityIds(appointmentInfo.getId()).getBytes(), 2, false);
                    break;
                }
                case "scheduleService/appointment/getAvailableAppointmentsAlert":{
                        System.out.println("Getting a number of all the available appointments");
                        int availableAppointments = appointmentService.getTotalnumberOfAvailableAppointments();
                        String appointmentString = String.valueOf(availableAppointments);
                        middleware.publish(PUBLISHED_AVAILABLE_APPOINTMENT_COUNT_TOPIC, appointmentString.getBytes(), 2, false);
                        totalMsgSent++;
                        System.out.println("PUBLISHING TOTAL NUMBER OF AVAILABLE APPOINTMENTS TO FRONTEND");
                    break;
                }
                case "scheduleService/totalMsgReceivedAlert" :{
                    System.out.println("PUBLISHING TOTAL MESSAGES RECEIVED: " + this.totalMsgReceived);
                    String msgReceivedString = String.valueOf(totalMsgReceived);
                    middleware.publish(PUBLISHED_TOTAL_MSG_RECEIVED, msgReceivedString.getBytes() , 2, false);
                    totalMsgSent++;
                    break;
                }
                case "scheduleService/totalMsgSentAlert":{
                    totalMsgSent++;
                    System.out.println("PUBLISHING TOTAL MESSAGES SENT: " + this.totalMsgSent);
                    String msgSentString = String.valueOf(totalMsgSent);
                    middleware.publish(PUBLISHED_TOTAL_MSG_SENT, msgSentString.getBytes(), 2, false);
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

    public String publishEntityIds(String appointmentId){
        AppointmentSchema appointment = appointmentService.getApppoinment(appointmentId);
        System.out.println("THIS IS THE APPOINTMENT IS BEING SENT TO THE AUTHENTICATION: " + appointment.toString());
        return appointment.toString();
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
