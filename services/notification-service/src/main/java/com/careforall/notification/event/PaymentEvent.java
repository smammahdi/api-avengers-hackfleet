package com.careforall.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Event
 *
 * Event received from Payment Service via RabbitMQ for payment processing events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paymentId;
    private Long donationId;
    private Long donorId;
    private String donorEmail;
    private String donorName;
    private Long campaignId;
    private String campaignName;
    private BigDecimal amount;
    private String status; // SUCCESS, FAILED, etc.
    private String failureReason;
    private LocalDateTime timestamp;
}
