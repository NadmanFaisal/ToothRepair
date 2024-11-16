package main.java.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MQTTSubscriber {

    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String SUBSCRIBED_TOPIC = "test/Clinic";

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
                    
                    System.out.println(message);

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