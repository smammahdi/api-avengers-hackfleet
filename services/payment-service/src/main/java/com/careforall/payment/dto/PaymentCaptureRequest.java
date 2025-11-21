package com.careforall.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payment Capture Request DTO
 *
 * Sent from Payment Service to Banking Service for capture
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
