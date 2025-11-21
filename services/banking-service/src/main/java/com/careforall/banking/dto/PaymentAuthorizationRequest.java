package com.careforall.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payment Authorization Request DTO
 *
 * Received from Payment Service via RabbitMQ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAuthorizationRequest {
    private String paymentId;
    private String donorEmail;
    private Long userId; // null for guest donations
    private BigDecimal amount;
    private String idempotencyKey;
}
