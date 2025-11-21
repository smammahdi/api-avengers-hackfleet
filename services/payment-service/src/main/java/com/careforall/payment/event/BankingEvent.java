package com.careforall.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Banking Event DTO
 *
 * Received from Banking Service via RabbitMQ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankingEvent {
    private String eventType; // PAYMENT_AUTHORIZED, PAYMENT_CAPTURED, PAYMENT_FAILED
    private String paymentId;
    private String transactionId; // Banking transaction ID
    private BigDecimal amount;
    private String status; // SUCCESS, FAILED
    private String failureReason;
    private LocalDateTime timestamp;
}
