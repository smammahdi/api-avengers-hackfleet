package com.careforall.payment.entity;

import com.careforall.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment Entity
 *
 * Represents a payment transaction with idempotency support and state machine.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
    @Index(name = "idx_donation_id", columnList = "donation_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true, length = 100)
    private String paymentId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "donation_id", nullable = false)
    private Long donationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "payment_metadata", joinColumns = @JoinColumn(name = "payment_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", length = 1000)
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "idempotency_expires_at", nullable = false)
    private LocalDateTime idempotencyExpiresAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (idempotencyExpiresAt == null) {
            // Set idempotency expiration to 24 hours from creation
            idempotencyExpiresAt = now.plusHours(24);
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add metadata entry
     */
    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    /**
     * Increment attempt count
     */
    public void incrementAttemptCount() {
        this.attemptCount++;
    }

    /**
     * Check if idempotency window has expired
     */
    public boolean isIdempotencyExpired() {
        return LocalDateTime.now().isAfter(idempotencyExpiresAt);
    }
}
