package com.ecommerce.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server - Service Discovery
 *
 * This server acts as a registry where all microservices register themselves.
 * Other services can discover and communicate with each other through this registry.
 *
 * Port: 8761
 * Dashboard: http://localhost:8761
 *
 * CI/CD Pipeline: Multi-stage build with unit tests, integration tests, and code quality checks
 *
 * @author API Avengers Team - v1.1
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
