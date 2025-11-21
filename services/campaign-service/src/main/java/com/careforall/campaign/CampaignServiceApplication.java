package com.careforall.campaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Campaign Service Application
 *
 * This service manages fundraising campaigns for the Care for All platform.
 *
 * Key Features:
 * - Campaign CRUD operations
 * - Campaign search and filtering by category
 * - Organizer management
 * - Donation tracking and goal monitoring
 * - Event publishing via RabbitMQ
 * - PostgreSQL database with JPA
 * - Service discovery with Eureka
 * - Distributed tracing with Zipkin
 *
 * CI/CD Pipeline: Multi-stage build with unit tests, integration tests, and code quality checks
 *
 * @author API Avengers Team - v1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CampaignServiceApplication {

    public static final String VERSION = "2.0.0-FINAL";

    public static void main(String[] args) {
        SpringApplication.run(CampaignServiceApplication.class, args);
        System.out.println("Campaign Service v" + VERSION + " started successfully");
    }
}
// CI/CD workflow test
