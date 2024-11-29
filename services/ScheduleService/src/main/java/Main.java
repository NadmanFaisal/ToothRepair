package main.java;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {
    
    public static void main(String[] args) {
        System.out.println("Print pls work cause im giving up");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(Main.class, args);

    }
    
}
