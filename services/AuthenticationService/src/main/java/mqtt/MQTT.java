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

import main.java.service.PatientService;

@Component
public class MQTT implements MqttCallback {
    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "test/patientList";
    private final PatientService patientService;
    private static final String SUBSCRIBED_TOPIC = "test/patientAlert";
    private ExecutorService thread = Executors.newSingleThreadExecutor();
    private final IMqttClient middleware;


    @Autowired
    public MQTT(PatientService patientSerivce) throws MqttException{
        this.patientService = patientSerivce;
        middleware = new MqttClient(BROKER_URL, CLIENT_ID);
        middleware.connect();
        middleware.setCallback(this);
        this.subscribeToTopics();
    }

    
    private void subscribeToTopics() {
        while(middleware.isConnected()){
        thread.submit(()-> {
            try {
                middleware.subscribe(SUBSCRIBED_TOPIC, 1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    }

    
    private void publishPatientList(){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String patientListJson = "My Name is Vaibhav and here are the objects...."+ objectMapper.writeValueAsString(this.patientService.getAllPatients());

            //Publish the payload as bytes to the topic.
            middleware.publish(PUBLISHED_TOPIC, patientListJson.getBytes(), 1, false);
            System.out.println(this.patientService.getAllPatients().toString()+ "Has been published");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 


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


    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        
        String stringMessage = message.toString(); 
                   
        System.out.println("Message recieved: " + stringMessage);
        if(stringMessage.equals("Get Patients")){
            this.publishPatientList();
        }
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Delivery Complete");
    }

    







}
