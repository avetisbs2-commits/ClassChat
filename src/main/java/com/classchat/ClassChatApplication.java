package com.classchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the embedded web server (HTTP + WebSocket). Run this class, or use ./mvnw spring-boot:run */
@SpringBootApplication
public class ClassChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassChatApplication.class, args);
    }
}
