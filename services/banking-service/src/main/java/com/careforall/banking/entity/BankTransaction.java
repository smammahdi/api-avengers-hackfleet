package com.careforall.banking.entity;

import com.careforall.banking.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bank Transaction Entity
 *
 * Audit trail for all banking operations
 */
@Entity
@Table(name = "bank_transactions", indexes = {
    @Index(name = "idx_transaction_account", columnList = "account_id"),
    @Index(name = "idx_transaction_reference", columnList = "external_reference"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "external_reference", nullable = false, length = 255)
    private String externalReference; // Payment ID from Payment Service

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
