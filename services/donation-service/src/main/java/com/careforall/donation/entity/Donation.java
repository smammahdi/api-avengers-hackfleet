package com.careforall.donation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Donation Entity - Matches insights.txt schema
 *
 * Supports:
 * - Guest donations (userId can be NULL)
 * - State machine: CREATED -> AUTHORIZED -> CAPTURED
 * - Idempotency via transaction_id
 * - Optimistic locking with @Version
 */
@Entity
@Table(name = "donations", indexes = {
    @Index(name = "idx_donation_campaign", columnList = "campaign_id"),
    @Index(name = "idx_donation_user", columnList = "user_id"),
    @Index(name = "idx_donation_email", columnList = "donor_email"),
    @Index(name = "idx_donation_transaction", columnList = "transaction_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Payment states: CREATED -> AUTHORIZED -> CAPTURED
     * CREATED: User clicked "Donate", not yet paid
     * AUTHORIZED: Bank approved, money on hold
     * CAPTURED: Money transferred to charity account
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DonationStatus status;

    /**
     * Transaction ID from Payment Gateway
     * Used for idempotency checks
     */
    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    /**
     * Always required - for guest donations or registered users
     */
    @Column(name = "donor_email", nullable = false, length = 255)
    private String donorEmail;

    /**
     * Donor name (optional, can be null for anonymous donations)
     */
    @Column(name = "donor_name", length = 255)
    private String donorName;

    /**
     * NULL for guest donations
     * Populated when user registers with same email
     */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    /**
     * Payment method used (e.g., CREDIT_CARD, DEBIT_CARD, PAYPAL)
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * Optional message from donor
     */
    @Column(name = "message", length = 1000)
    private String message;

    /**
     * Whether donation is anonymous
     */
    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    /**
     * Timestamp when donation was completed (CAPTURED)
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Optimistic locking - incremented on each update
     * Prevents concurrent modification issues
     */
    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * State transition helper methods
     */
    public void authorize() {
        if (this.status != DonationStatus.CREATED) {
            throw new IllegalStateException("Cannot authorize donation in state: " + this.status);
        }
        this.status = DonationStatus.AUTHORIZED;
    }

    public void capture(String txnId) {
        if (this.status != DonationStatus.AUTHORIZED) {
            throw new IllegalStateException("Cannot capture donation in state: " + this.status);
        }
        this.status = DonationStatus.CAPTURED;
        this.transactionId = txnId;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Check if donation is from guest user
     */
    public boolean isGuestDonation() {
        return this.userId == null;
    }

    /**
     * Link guest donation to registered user
     */
    public void linkToUser(Long registeredUserId) {
        if (!isGuestDonation()) {
            throw new IllegalStateException("Donation already linked to user: " + this.userId);
        }
        this.userId = registeredUserId;
    }
}
