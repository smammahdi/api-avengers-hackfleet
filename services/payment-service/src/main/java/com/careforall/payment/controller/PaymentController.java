package com.careforall.payment.controller;

import com.careforall.payment.dto.PaymentRequest;
import com.careforall.payment.dto.PaymentResponse;
import com.careforall.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Payment Controller
 *
 * REST API endpoints for donation payment processing with idempotency support.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * Process payment for a donation
     *
     * This endpoint supports idempotency via the idempotencyKey in the request.
     * If a payment with the same idempotencyKey was processed within the last 24 hours,
     * the original result will be returned.
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            logger.info("Received payment request for donation: {}", request.getDonationId());
            PaymentResponse response = paymentService.processPayment(request);

            if (response.isFromCache()) {
                logger.info("Returning cached payment result (idempotent request)");
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to process payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse("Payment processing failed: " + e.getMessage()));
        }
    }

    /**
     * Get payment by payment ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable String paymentId) {
        try {
            return paymentService.getPaymentById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error retrieving payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Get payment by donation ID
     */
    @GetMapping("/donation/{donationId}")
    public ResponseEntity<?> getPaymentByDonation(@PathVariable Long donationId) {
        try {
            return paymentService.getPaymentByDonationId(donationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error retrieving payment for donation {}: {}", donationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "hf-payment-service");
        response.put("features", "idempotency,state-machine,retry-logic");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create error response
     */
    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        return error;
    }
}
