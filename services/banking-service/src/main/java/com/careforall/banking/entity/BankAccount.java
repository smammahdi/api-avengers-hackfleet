package com.careforall.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bank Account Entity
 *
 * Represents a user's bank account with:
 * - Available balance (can be spent)
 * - Locked balance (authorized but not captured)
 * - Total balance = available + locked
 */
@Entity
@Table(name = "bank_accounts", indexes = {
    @Index(name = "idx_account_user", columnList = "user_id"),
    @Index(name = "idx_account_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "account_holder_name", nullable = false, length = 255)
    private String accountHolderName;

    /**
     * Available balance that can be spent
     */
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    /**
     * Locked balance (authorized but not yet captured)
     */
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal lockedBalance = BigDecimal.ZERO;

    /**
     * Optimistic locking
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
     * Get total balance (available + locked)
     */
    public BigDecimal getTotalBalance() {
        return availableBalance.add(lockedBalance);
    }

    /**
     * Lock funds for authorization
     */
    public void lockFunds(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance. Available: " + availableBalance + ", Required: " + amount);
        }
        this.availableBalance = availableBalance.subtract(amount);
        this.lockedBalance = lockedBalance.add(amount);
    }

    /**
     * Capture locked funds (complete transaction)
     */
    public void captureFunds(BigDecimal amount) {
        if (lockedBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient locked balance. Locked: " + lockedBalance + ", Required: " + amount);
        }
        this.lockedBalance = lockedBalance.subtract(amount);
        // Funds leave the system (transferred to charity)
    }

    /**
     * Release locked funds (cancel authorization)
     */
    public void releaseFunds(BigDecimal amount) {
        if (lockedBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient locked balance. Locked: " + lockedBalance + ", Required: " + amount);
        }
        this.lockedBalance = lockedBalance.subtract(amount);
        this.availableBalance = availableBalance.add(amount);
    }

    /**
     * Add funds to available balance (refund or deposit)
     */
    public void addFunds(BigDecimal amount) {
        this.availableBalance = availableBalance.add(amount);
    }
}
