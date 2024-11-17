package main.java.mqtt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        try {
            SpringApplication.run(Main.class, args);
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
