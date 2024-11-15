//This code is sourced by ChatGPT

package main.java.mqtt;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

public class MQTTSubscriber {

    private static final String BROKER_URL = "tcp://localhost:1883";  // Replace with your broker address
    private static final String CLIENT_ID = "JavaServiceClient";      // Unique client ID
    private static final String SUBSCRIBED_TOPIC = "test/Authentication";

    private IMqttClient client;

    public MQTTSubscriber() throws MqttException {
        // Initialize client with broker URL and client ID
        client = new MqttClient(BROKER_URL, CLIENT_ID);
    }

    public void connect() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(0);
        
        // Connect to the broker
        client.connect(options);
        System.out.println("Connected to broker: " + BROKER_URL);
    }

    public void subscribe() throws MqttException {
        client.subscribe(SUBSCRIBED_TOPIC, new IMqttMessageListener() {
            @Override
            public void messageArrived(String SUBSCRIBED_TOPIC, MqttMessage message) throws Exception {
                System.out.println("Received message: " + message.toString() + " from SUBSCRIBED_TOPIC: " + SUBSCRIBED_TOPIC);
                // Add logic to process the message and interact with your backend
            }
        });
        System.out.println("Subscribed to SUBSCRIBED_TOPIC: " + SUBSCRIBED_TOPIC);
    }

    public void disconnect() throws MqttException {
        if (client.isConnected()) {
            client.disconnect();
            System.out.println("Disconnected from broker");
        }
    }

    public static void main(String[] args) {
        try {
            MQTTSubscriber mqttService = new MQTTSubscriber();
            mqttService.connect();
            mqttService.subscribe();
            // Keep the client running to listen for incoming messages
            Thread.sleep(5000);

            mqttService.disconnect();
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}