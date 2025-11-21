package com.careforall.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Donation Event
 *
 * Event received from donation-service when a donation is created.
 * This triggers the payment processing flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long donationId;
    private Long campaignId;
    private Long userId;
    private BigDecimal amount;
    private String idempotencyKey;
    private String paymentMethod;
    private LocalDateTime timestamp;
}
