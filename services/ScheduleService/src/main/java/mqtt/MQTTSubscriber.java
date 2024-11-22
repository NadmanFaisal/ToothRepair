package main.java.mqtt;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import main.java.controller.AppointmentController;
import main.java.db.AppointmentSchema;
import main.java.service.BookingService;


public class MQTTSubscriber {

    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String SUBSCRIBED_TOPIC = "test/Schedule";
    private ExecutorService thread = Executors.newSingleThreadExecutor();

    @Autowired
    private AppointmentController appointmentController;

    // Method to subscribe to a topic
    public MQTTSubscriber() {
        try {
            // Initialise MQTT client and connect to broker using broker and client id
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            MqttConnectOptions connection = new MqttConnectOptions();
            client.connect(connection);
            System.out.println("Connected");

            // Defines what will happen when the connection is lost, message is arrived and delivery is completed
            client.setCallback(new MqttCallback() {
                // Method to show in case connection is lost
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection is lost");
                }

                // Method to save message from the MQTT broker into the JSON file
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                   System.out.println("Message recieved: " + message.toString());
                    
                    if (topic.equals(SUBSCRIBED_TOPIC)) {
                        List<AppointmentSchema> allAppointments = appointmentController.getAllAppointments();

                        StringBuilder appointmentListResponse = new StringBuilder();
                        for (AppointmentSchema appointment : allAppointments) {
                            appointmentListResponse
                                .append("ID: ").append(appointment.getId());
                        }
                        MqttMessage responseMessage = new MqttMessage(appointmentListResponse.toString().getBytes());
                        client.publish("test/ScheduleList", responseMessage);
                        System.out.println("Appointment list sent to response topic");
                    }


                }

                // Method to see that delivery is completed
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Complete");
                }
            });
            // Subscribe to different topics to receive message
            client.subscribe(SUBSCRIBED_TOPIC);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed");
        }
    }
}