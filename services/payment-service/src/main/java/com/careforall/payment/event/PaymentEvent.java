package com.careforall.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Event
 *
 * Event published to RabbitMQ when payment is processed (completed or failed).
 * Used to notify donation-service about payment status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paymentId;
    private Long donationId;
    private Long userId;
    private BigDecimal amount;
    private String status; // COMPLETED or FAILED
    private LocalDateTime timestamp;
}
