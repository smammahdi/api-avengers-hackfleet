package com.careforall.donation.entity;

/**
 * Donation Status Enum - Matches insights.txt schema
 *
 * State Machine Flow:
 * CREATED: User clicked "Donate", order created, not yet paid
 * AUTHORIZED: Payment authorized, money on hold
 * CAPTURED: Money transferred to charity account (final state)
 *
 * Invalid Transitions (Rejected by State Machine):
 * - CAPTURED -> AUTHORIZED (backward move)
 * - AUTHORIZED -> CREATED (backward move)
 * - CAPTURED -> CREATED (backward move)
 */
public enum DonationStatus {
    /**
     * Initial state: Donation record created, awaiting payment
     */
    CREATED,

    /**
     * Payment gateway authorized the transaction
     * Money is on hold but not yet captured
     */
    AUTHORIZED,

    /**
     * Final state: Money successfully transferred
     * Donation is complete
     */
    CAPTURED
}
