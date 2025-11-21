package com.careforall.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AnalyticsServiceApplication {

    public static final String VERSION = "2.0.0-FINAL";

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
        System.out.println("Analytics Service v" + VERSION + " started successfully");
    }
}
