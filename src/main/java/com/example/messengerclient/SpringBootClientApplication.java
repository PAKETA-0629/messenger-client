package com.example.messengerclient;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootClientApplication {

    public static void main(String[] args) {
        // This is how normal Spring Boot app would be launched
        //SpringApplication.run(JavafxWeaverExampleApplication.class, args);
        //SpringApplication.run(SpringBootClientApplication.class, args);
        Application.launch(ClientApplicationInitializer.class, args);
    }
}
