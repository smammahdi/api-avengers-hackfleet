package com.careforall.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Payment Request DTO
 *
 * Used for donation payment processing with idempotency support.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotNull(message = "Donation ID is required")
    private Long donationId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String paymentMethod; // e.g., "credit_card", "debit_card", "bank_transfer"

    private Map<String, String> metadata; // Additional payment metadata
}
