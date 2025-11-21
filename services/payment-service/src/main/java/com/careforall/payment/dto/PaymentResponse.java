package com.careforall.payment.dto;

import com.careforall.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Payment Response DTO
 *
 * Returned after payment processing with idempotency and state information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String paymentId;
    private String idempotencyKey;
    private Long donationId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String message;
    private Map<String, String> metadata;
    private Integer attemptCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean fromCache; // Indicates if this was returned from idempotency cache

    /**
     * Create success response
     */
    public static PaymentResponse success(String paymentId, String idempotencyKey,
                                         Long donationId, BigDecimal amount) {
        return PaymentResponse.builder()
            .paymentId(paymentId)
            .idempotencyKey(idempotencyKey)
            .donationId(donationId)
            .amount(amount)
            .status(PaymentStatus.COMPLETED)
            .message("Payment processed successfully")
            .fromCache(false)
            .build();
    }

    /**
     * Create failure response
     */
    public static PaymentResponse failure(String paymentId, String idempotencyKey,
                                         Long donationId, BigDecimal amount, String errorMessage) {
        return PaymentResponse.builder()
            .paymentId(paymentId)
            .idempotencyKey(idempotencyKey)
            .donationId(donationId)
            .amount(amount)
            .status(PaymentStatus.FAILED)
            .message("Payment failed")
            .errorMessage(errorMessage)
            .fromCache(false)
            .build();
    }
}
