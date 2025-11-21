package com.careforall.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payment Capture Request DTO
 *
 * Received from Payment Service via RabbitMQ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCaptureRequest {
    private String paymentId;
    private String donorEmail;
    private Long userId;
    private BigDecimal amount;
}
