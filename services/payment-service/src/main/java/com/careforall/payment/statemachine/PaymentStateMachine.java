package com.careforall.payment.statemachine;

import com.careforall.payment.entity.Payment;
import com.careforall.payment.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Payment State Machine - Solves Problem #3 from insights.txt
 *
 * Prevents invalid state transitions, especially backward moves
 * caused by out-of-order webhooks.
 *
 * Valid transitions:
 * - CREATED -> AUTHORIZED (payment gateway approves)
 * - AUTHORIZED -> CAPTURED (money transferred)
 *
 * REJECTED transitions (prevents the "CAPTURED becomes AUTHORIZED" glitch):
 * - CAPTURED -> AUTHORIZED ❌ (backward move)
 * - AUTHORIZED -> CREATED ❌ (backward move)
 * - CAPTURED -> CREATED ❌ (backward move)
 *
 * Problem Context:
 * Scenario: CAPTURED webhook arrives first (processed successfully)
 * Then: Delayed AUTHORIZED webhook arrives
 * Without State Machine: CAPTURED overwrites to AUTHORIZED (BUG!)
 * With State Machine: AUTHORIZED rejected because current state is CAPTURED (FIXED!)
 */
@Component
public class PaymentStateMachine {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStateMachine.class);

    // Define valid state transitions
    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        // CREATED can only transition to AUTHORIZED
        VALID_TRANSITIONS.put(PaymentStatus.CREATED, EnumSet.of(PaymentStatus.AUTHORIZED));

        // AUTHORIZED can only transition to CAPTURED
        VALID_TRANSITIONS.put(PaymentStatus.AUTHORIZED, EnumSet.of(PaymentStatus.CAPTURED));

        // CAPTURED is terminal - no further transitions allowed
        VALID_TRANSITIONS.put(PaymentStatus.CAPTURED, EnumSet.noneOf(PaymentStatus.class));
    }

    /**
     * Check if a state transition is valid
     *
     * @param currentStatus Current payment status
     * @param targetStatus Target payment status
     * @return true if transition is valid, false otherwise
     */
    public boolean canTransition(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            logger.warn("Invalid state transition attempt: null status provided");
            return false;
        }

        // If already in target state, it's valid (idempotent webhook)
        if (currentStatus == targetStatus) {
            logger.debug("Payment already in target state {}, allowing idempotent transition", targetStatus);
            return true;
        }

        // Check if this is a backward transition (CRITICAL CHECK)
        if (targetStatus.getRank() < currentStatus.getRank()) {
            logger.warn("REJECTED backward transition: {} -> {} (rank {} -> {})",
                currentStatus, targetStatus, currentStatus.getRank(), targetStatus.getRank());
            return false;
        }

        Set<PaymentStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        boolean isAllowed = allowedTransitions != null && allowedTransitions.contains(targetStatus);

        if (!isAllowed) {
            logger.warn("Invalid state transition: {} -> {} not in allowed transitions: {}",
                currentStatus, targetStatus, allowedTransitions);
        }

        return isAllowed;
    }

    /**
     * Transition payment to a new status
     *
     * @param payment Payment entity
     * @param targetStatus Target status
     * @return true if transition was successful
     * @throws IllegalStateException if transition is invalid
     */
    public boolean transition(Payment payment, PaymentStatus targetStatus) {
        PaymentStatus currentStatus = payment.getStatus();

        if (currentStatus == targetStatus) {
            logger.debug("Payment {} already in status {} (idempotent webhook)",
                payment.getPaymentId(), targetStatus);
            return true;
        }

        if (!canTransition(currentStatus, targetStatus)) {
            String errorMessage = String.format(
                "REJECTED invalid state transition for payment %s: %s -> %s (backward or invalid move)",
                payment.getPaymentId(), currentStatus, targetStatus
            );
            logger.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        logger.info("✅ Valid transition for payment {}: {} -> {}",
            payment.getPaymentId(), currentStatus, targetStatus);

        payment.setStatus(targetStatus);
        return true;
    }

    /**
     * Transition to AUTHORIZED status
     *
     * @param payment Payment entity
     */
    public void transitionToAuthorized(Payment payment) {
        transition(payment, PaymentStatus.AUTHORIZED);
    }

    /**
     * Transition to CAPTURED status
     *
     * @param payment Payment entity
     */
    public void transitionToCaptured(Payment payment) {
        transition(payment, PaymentStatus.CAPTURED);
    }

    /**
     * Get all valid next states for a given status
     *
     * @param currentStatus Current payment status
     * @return Set of valid next states
     */
    public Set<PaymentStatus> getValidNextStates(PaymentStatus currentStatus) {
        return VALID_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(PaymentStatus.class));
    }

    /**
     * Check if a status is terminal (no further transitions possible)
     *
     * @param status Payment status to check
     * @return true if status is terminal
     */
    public boolean isTerminalState(PaymentStatus status) {
        return status != null && status.isTerminal();
    }
}