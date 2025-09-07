package com.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for the Banking System
 * 
 * This is the entry point of the Spring Boot banking application.
 * It includes caching support and auto-configuration for all components.
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@EnableCaching
public class BankingApplication {

    /**
     * Main method to start the Spring Boot application
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

}
