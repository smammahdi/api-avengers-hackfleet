package com.careforall.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Donation Event
 *
 * Event received from Donation Service via RabbitMQ when a donation is completed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long donationId;
    private Long donorId;
    private String donorName;
    private String donorEmail;
    private Long campaignId;
    private String campaignName;
    private BigDecimal amount;
    private String transactionId;
    private String status;
    private LocalDateTime timestamp;
}
