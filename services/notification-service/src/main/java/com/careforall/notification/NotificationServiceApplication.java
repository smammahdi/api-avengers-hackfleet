package com.careforall.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * HF Notification Service Application
 *
 * This service listens to RabbitMQ events and sends notifications for the CareForAll platform.
 *
 * Key Features:
 * - RabbitMQ message consumers for donation, campaign, and payment events
 * - Mock email notification service for donation receipts
 * - Campaign lifecycle notifications (created, completed)
 * - Payment failure notifications with retry instructions
 * - Asynchronous event processing
 * - Service discovery with Eureka
 * - Distributed tracing with Zipkin
 *
 * Event Types Handled:
 * - DONATION_COMPLETED: Send donation receipt to donor
 * - CAMPAIGN_CREATED: Notify platform admins
 * - CAMPAIGN_COMPLETED: Notify campaign organizer with final stats
 * - PAYMENT_FAILED: Notify donor with retry instructions
 *
 * @version 1.0.0
 * @author API Avengers Team - HF Platform
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {

    public static final String VERSION = "2.0.0-FINAL";

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
        System.out.println("Notification Service v" + VERSION + " started successfully");
    }
}
