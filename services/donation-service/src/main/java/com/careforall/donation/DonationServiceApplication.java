package com.careforall.donation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * HF Donation Service Application
 *
 * This service manages donations/pledges for humanitarian funding campaigns.
 *
 * Key Features:
 * - Donation creation and management
 * - Transactional Outbox Pattern for reliable event publishing
 * - Scheduled task (every 5s) to publish pending events to RabbitMQ
 * - Guaranteed eventual consistency without dual-write problems
 * - PostgreSQL database for donations and outbox events
 * - Service discovery with Eureka
 * - Distributed tracing with Zipkin
 * - Async event-driven architecture (no synchronous Feign calls)
 *
 * Transactional Outbox Pattern:
 * - Business entity (Donation) and event (OutboxEvent) saved in SAME transaction
 * - OutboxPublisher scheduled task polls outbox table every 5 seconds
 * - Events published to RabbitMQ asynchronously with retry logic
 * - No data loss even if message broker is temporarily unavailable
 *
 * @author API Avengers Team - v1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class DonationServiceApplication {

    public static final String VERSION = "2.0.0-FINAL";

    public static void main(String[] args) {
        SpringApplication.run(DonationServiceApplication.class, args);
        System.out.println("Donation Service v" + VERSION + " started successfully");
    }
}
