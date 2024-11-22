package main.java.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.service.BookingService;

public class MQTTPublisher {

    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "test/ScheduleList";
    private final BookingService bookingService;

    //Put them in a JSON format for the payload. (might be useful in the future)
    //private static final String userWeightAndHeight = "{"userWeight": "  + userWeight + "," + " "userHeight": " + userHeight + "}";

    @Autowired
    public MQTTPublisher(BookingService bookingService) {
        this.bookingService = bookingService;
        publishAppointmentList();
    }

    private void publishAppointmentList() {
        try {
             
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.connect();
            System.out.println("MQTTPublisher has been connected to: " + PUBLISHED_TOPIC);


            ObjectMapper objectMapper = new ObjectMapper();
            String patientListJson = objectMapper.writeValueAsString(this.bookingService.getAllAppointments());

            //Publish the payload as bytes to the topic.
            client.publish(PUBLISHED_TOPIC, patientListJson.getBytes(), 0, true);
            System.out.println(this.bookingService.getAllAppointments().toString()+ "Has been published");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

}