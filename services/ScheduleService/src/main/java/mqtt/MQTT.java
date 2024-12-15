package main.java.mqtt;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import main.java.db.AppointmentSchema;
import main.java.service.AppointmentService;

@Component
public class MQTT implements MqttCallback {
    private static final String [] BROKER_URLS = { "ssl://193a0f31e34647d9a74f1e130a9238ba.s1.eu.hivemq.cloud" ,"tcp://test.mosquitto.org", "tcp://broker.hivemq.com", "tcp://broker.emqx.io"};
    private static final String CLIENT_ID = "ScheduleClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "client/scheduleService/appointmentInfo";
    private final AppointmentService appointmentService; // CRUD Operations for the schedule database
    private static final String[] SUBSCRIBED_TOPICS = {"scheduleService/appointment/getAppointments",
     "scheduleService/appointment/createAppointment", "scheduleService/appointment/bookAppointment", "scheduleService/appointment/changeAppointmentStatus"}; 
    private ExecutorService threadPool; // thread to handle each subscribed topic
    private IMqttClient middleware; // MQTT client
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private MqttConnectOptions options = new MqttConnectOptions();
    private int currentBrokerIndex = 0;
    private boolean STRESS_TEST_MODE = true;


    
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
    private void publishAppointmentList(){
        try {
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
                   
            System.out.println("Message recieved: " + stringMessage);
            switch (topic) {
                case "scheduleService/appointment/getAppointments":
                    if(stringMessage.equals("Get Appointments")){
                        System.out.println("Will publish all appointments");
                        this.publishAppointmentList();
                    }   break;
                case "scheduleService/appointment/createAppointment":
                    {
                        System.out.println("Entered createAppointment if statement");
                        AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                        System.out.println(appointmentInfo.toString());
                        appointmentService.createAppointment(appointmentInfo);
                        break;
                    }
                case "scheduleService/appointment/bookAppointment":
                    {
                        System.out.println("Entered bookAppointment if statement");
                        AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                        System.out.println(appointmentInfo.toString());
                        appointmentService.bookAppointment(appointmentInfo);
                        break;
                    }
                case "scheduleService/appointment/changeAppointmentStatus":
                    {
                        System.out.println("Entered changeAppointmentStatus if statement");
                        AppointmentSchema appointmentInfo = objectMapper.readValue(stringMessage, AppointmentSchema.class);
                        System.out.println(appointmentInfo.toString());
                        appointmentService.changeAppointmentStatus(appointmentInfo);
                        break;
                    }
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
