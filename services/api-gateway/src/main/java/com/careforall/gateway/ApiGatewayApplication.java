package com.careforall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application
 *
 * This is the entry point for all client requests to the e-commerce microservices platform.
 * It handles JWT authentication, request routing, and adds user context headers to downstream services.
 *
 * Key Features:
 * - JWT token validation
 * - Service discovery with Eureka
 * - Request routing to microservices
 * - Distributed tracing with Zipkin
 * - Metrics collection for Prometheus
 *
 * CI/CD Pipeline: Multi-stage build with unit tests, integration tests, and code quality checks
 *
 * @author API Avengers Team - v1.1
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
