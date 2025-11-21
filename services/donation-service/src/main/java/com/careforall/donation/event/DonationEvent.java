package com.careforall.donation.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Donation Event
 *
 * Event published to RabbitMQ when donation status changes.
 * This is the actual payload that will be sent via the message broker.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationEvent {

    private String donationId; // UUID as String
    private Long campaignId;   // Campaign still uses Long
    private Long userId;       // User ID (can be null for guest donations)
    private String donorEmail; // Always present
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private String eventType;
    private LocalDateTime timestamp;

    /**
     * Create a DONATION_CREATED event
     */
    public static DonationEvent created(String donationId, Long campaignId, Long userId,
                                       String donorEmail, BigDecimal amount) {
        return new DonationEvent(
            donationId,
            campaignId,
            userId,
            donorEmail,
            amount,
            "CREATED",
            null,
            "DONATION_CREATED",
            LocalDateTime.now()
        );
    }

    /**
     * Create a DONATION_AUTHORIZED event
     */
    public static DonationEvent authorized(String donationId, Long campaignId, Long userId,
                                          String donorEmail, BigDecimal amount) {
        return new DonationEvent(
            donationId,
            campaignId,
            userId,
            donorEmail,
            amount,
            "AUTHORIZED",
            null,
            "DONATION_AUTHORIZED",
            LocalDateTime.now()
        );
    }

    /**
     * Create a DONATION_CAPTURED event (final successful state)
     */
    public static DonationEvent captured(String donationId, Long campaignId, Long userId,
                                        String donorEmail, BigDecimal amount, String transactionId) {
        return new DonationEvent(
            donationId,
            campaignId,
            userId,
            donorEmail,
            amount,
            "CAPTURED",
            transactionId,
            "DONATION_CAPTURED",
            LocalDateTime.now()
        );
    }
}
