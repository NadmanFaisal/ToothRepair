package main.java.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MQTTPublisher {

    private static final String BROKER_URL = "tcp://test.mosquitto.org";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String PUBLISHED_TOPIC = "test/Authentication";

    //Put them in a JSON format for the payload. (might be useful in the future)
    //private static final String userWeightAndHeight = "{"userWeight": "  + userWeight + "," + " "userHeight": " + userHeight + "}";

    public MQTTPublisher() {
        try {
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.connect();
            System.out.println("MQTTPublisher has been connected!");

            //Publish the payload as bytes to the topic.
            client.publish(PUBLISHED_TOPIC, "{name: Vanis}".getBytes(), 0, false);
            System.out.println("banana was published!");
            

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}