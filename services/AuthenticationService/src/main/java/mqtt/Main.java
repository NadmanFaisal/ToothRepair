package main.java.mqtt;

public class Main {
    public static void main(String[] args) {
        try {
        
            // Keep the client running to listen for incoming messages
            new MQTTSubscriber();
            // Keep the client running to listen for incoming messages

            new MQTTPublisher();
            
            Thread.sleep(5000);
        
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    
}
