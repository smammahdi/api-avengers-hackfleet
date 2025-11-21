package com.careforall.banking.enums;

/**
 * Transaction Type Enum
 *
 * Defines types of banking transactions:
 * - AUTHORIZATION: Funds locked but not transferred
 * - CAPTURE: Funds actually transferred
 * - REFUND: Funds returned to user
 * - CANCELLATION: Authorization cancelled, funds released
 */
public enum TransactionType {
    AUTHORIZATION("Funds locked for pending payment"),
    CAPTURE("Funds transferred successfully"),
    REFUND("Funds returned to account"),
    CANCELLATION("Authorization cancelled, funds released");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
