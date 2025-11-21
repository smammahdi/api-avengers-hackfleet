package com.careforall.payment.enums;

/**
 * Payment Status Enum - Matches insights.txt schema
 *
 * Defines the possible states in the payment processing state machine.
 * Valid transitions:
 * - PENDING -> CREATED (payment order initialized)
 * - CREATED -> AUTHORIZED (payment gateway approves)
 * - AUTHORIZED -> CAPTURED (money transferred)
 * - CAPTURED -> COMPLETED (final success state)
 * - Any state -> FAILED (on errors)
 *
 * CRITICAL: The state machine MUST reject backward transitions
 * - CAPTURED -> AUTHORIZED = REJECTED ❌
 * - AUTHORIZED -> CREATED = REJECTED ❌
 *
 * Problem Context (from insights.txt):
 * When webhooks arrive out of order (CAPTURED before AUTHORIZED),
 * the system must prevent CAPTURED status from being overwritten
 * by the delayed AUTHORIZED webhook.
 */
public enum PaymentStatus {
    PENDING("Payment pending, waiting to be processed"),
    CREATED("Payment order created, awaiting authorization"),
    AUTHORIZED("Payment authorized, money on hold"),
    CAPTURED("Payment captured, money transferred"),
    COMPLETED("Payment completed successfully"),
    FAILED("Payment failed");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this status is a terminal state
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CAPTURED;
    }

    /**
     * Get the ordinal ranking for preventing backward transitions
     * Higher ordinal = more advanced state
     */
    public int getRank() {
        return this.ordinal();
    }
}
