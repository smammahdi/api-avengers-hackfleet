package com.careforall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * HF Payment Service Application
 *
 * This service handles donation payment processing with advanced features:
 * - Idempotency support (24-hour window)
 * - State machine for payment status transitions
 * - Retry logic with exponential backoff (max 3 attempts)
 * - Event-driven architecture with RabbitMQ
 * - H2 database for payment persistence
 *
 * Key Features:
 * - Processes payments for donations
 * - Listens to DONATION_CREATED events
 * - Publishes PAYMENT_COMPLETED or PAYMENT_FAILED events
 * - Service discovery with Eureka
 * - Distributed tracing with Zipkin
 *
 * CI/CD Pipeline: Multi-stage build with unit tests, integration tests, and code quality checks
 *
 * @author API Avengers Team - v1.2 (HopeFund)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentServiceApplication {

    public static final String VERSION = "2.0.0-FINAL";

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("Payment Service v" + VERSION + " started successfully");
    }
}
// CI/CD workflow test
