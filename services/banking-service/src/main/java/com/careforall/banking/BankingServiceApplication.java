package com.careforall.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * HF Banking Service Application
 *
 * This service simulates a banking system that processes payment requests:
 * - Maintains user account balances
 * - Processes authorization requests (check balance + lock funds)
 * - Processes capture requests (transfer funds)
 * - Handles refunds and cancellations
 *
 * Key Features:
 * - Event-driven architecture with RabbitMQ
 * - Listens to PAYMENT_AUTHORIZATION_REQUESTED events from Payment Service
 * - Publishes PAYMENT_AUTHORIZED/PAYMENT_FAILED events back to Payment Service
 * - PostgreSQL database for account and transaction persistence
 * - Resilience4j for fault tolerance (Circuit Breaker, Retry, Bulkhead)
 * - Service discovery with Eureka
 * - Distributed tracing with Zipkin
 * - Metrics export to Prometheus
 *
 * Payment Flow:
 * 1. Donation Service → Payment Service (create payment)
 * 2. Payment Service → Banking Service (authorize payment)
 * 3. Banking Service checks balance, locks funds
 * 4. Banking Service → Payment Service (authorization result)
 * 5. Payment Service → Banking Service (capture payment)
 * 6. Banking Service transfers funds
 * 7. Banking Service → Payment Service (capture result)
 * 8. Payment Service → Donation Service (final status)
 *
 * State Machine: CREATED → AUTHORIZED → CAPTURED/FAILED
 *
 * @author API Avengers Team - v2.0 (HopeFund)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BankingServiceApplication {

    public static final String VERSION = "2.0.0-FINAL";

    public static void main(String[] args) {
        SpringApplication.run(BankingServiceApplication.class, args);
        System.out.println("Banking Service v" + VERSION + " started successfully");
    }
}
// CI/CD workflow test
