package com.careforall.payment.statemachine;

import com.careforall.payment.entity.Payment;
import com.careforall.payment.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Payment State Machine Tests
 *
 * Tests the state transition logic for payment processing.
 * This tests the solution to Problem #3 from insights.txt:
 * - Prevents backward state transitions
 * - Handles out-of-order webhooks correctly
 * - Allows idempotent webhook processing
 */
class PaymentStateMachineTest {

    private PaymentStateMachine stateMachine;
    private Payment payment;

    @BeforeEach
    void setUp() {
        stateMachine = new PaymentStateMachine();
        payment = Payment.builder()
            .paymentId("PAY-TEST-001")
            .status(PaymentStatus.CREATED)
            .build();
    }

    // =========================================================================
    // VALID TRANSITION TESTS
    // =========================================================================

    @Test
    @DisplayName("Valid transition: CREATED -> AUTHORIZED")
    void testValidTransitionCreatedToAuthorized() {
        assertTrue(stateMachine.canTransition(PaymentStatus.CREATED, PaymentStatus.AUTHORIZED));
        stateMachine.transitionToAuthorized(payment);
        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
    }

    @Test
    @DisplayName("Valid transition: AUTHORIZED -> CAPTURED")
    void testValidTransitionAuthorizedToCaptured() {
        payment.setStatus(PaymentStatus.AUTHORIZED);
        assertTrue(stateMachine.canTransition(PaymentStatus.AUTHORIZED, PaymentStatus.CAPTURED));
        stateMachine.transitionToCaptured(payment);
        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
    }

    @Test
    @DisplayName("Idempotent transition: Same state allowed")
    void testIdempotentTransition() {
        payment.setStatus(PaymentStatus.AUTHORIZED);

        // Same state transition should be allowed (idempotent webhook)
        assertTrue(stateMachine.canTransition(PaymentStatus.AUTHORIZED, PaymentStatus.AUTHORIZED));
        assertTrue(stateMachine.transition(payment, PaymentStatus.AUTHORIZED));
        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
    }

    // =========================================================================
    // BACKWARD TRANSITION PREVENTION TESTS (CRITICAL!)
    // =========================================================================

    @Test
    @DisplayName("REJECT backward transition: CAPTURED -> AUTHORIZED (Problem #3 fix)")
    void testRejectBackwardTransitionCapturedToAuthorized() {
        payment.setStatus(PaymentStatus.CAPTURED);

        // This is the CRITICAL test - prevents the bug from problem statement
        assertFalse(stateMachine.canTransition(PaymentStatus.CAPTURED, PaymentStatus.AUTHORIZED));

        assertThrows(IllegalStateException.class, () -> {
            stateMachine.transitionToAuthorized(payment);
        });

        // Payment should remain in CAPTURED state
        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
    }

    @Test
    @DisplayName("REJECT backward transition: AUTHORIZED -> CREATED")
    void testRejectBackwardTransitionAuthorizedToCreated() {
        payment.setStatus(PaymentStatus.AUTHORIZED);

        assertFalse(stateMachine.canTransition(PaymentStatus.AUTHORIZED, PaymentStatus.CREATED));

        assertThrows(IllegalStateException.class, () -> {
            stateMachine.transition(payment, PaymentStatus.CREATED);
        });

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
    }

    @Test
    @DisplayName("REJECT backward transition: CAPTURED -> CREATED")
    void testRejectBackwardTransitionCapturedToCreated() {
        payment.setStatus(PaymentStatus.CAPTURED);

        assertFalse(stateMachine.canTransition(PaymentStatus.CAPTURED, PaymentStatus.CREATED));
        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
    }

    // =========================================================================
    // INVALID SKIP TRANSITION TESTS
    // =========================================================================

    @Test
    @DisplayName("REJECT skipping state: CREATED -> CAPTURED")
    void testInvalidTransitionCreatedToCaptured() {
        assertFalse(stateMachine.canTransition(PaymentStatus.CREATED, PaymentStatus.CAPTURED));

        assertThrows(IllegalStateException.class, () -> {
            stateMachine.transition(payment, PaymentStatus.CAPTURED);
        });
    }

    // =========================================================================
    // TERMINAL STATE TESTS
    // =========================================================================

    @Test
    @DisplayName("CAPTURED is a terminal state")
    void testCapturedIsTerminalState() {
        assertTrue(stateMachine.isTerminalState(PaymentStatus.CAPTURED));

        payment.setStatus(PaymentStatus.CAPTURED);
        Set<PaymentStatus> nextStates = stateMachine.getValidNextStates(PaymentStatus.CAPTURED);

        assertTrue(nextStates.isEmpty(), "Terminal state should have no valid transitions");
    }

    @Test
    @DisplayName("COMPLETED is a terminal state")
    void testCompletedIsTerminalState() {
        assertTrue(stateMachine.isTerminalState(PaymentStatus.COMPLETED));
    }

    @Test
    @DisplayName("FAILED is a terminal state")
    void testFailedIsTerminalState() {
        assertTrue(stateMachine.isTerminalState(PaymentStatus.FAILED));
    }

    // =========================================================================
    // VALID NEXT STATES TESTS
    // =========================================================================

    @Test
    @DisplayName("Get valid next states from CREATED")
    void testGetValidNextStatesFromCreated() {
        Set<PaymentStatus> nextStates = stateMachine.getValidNextStates(PaymentStatus.CREATED);

        assertEquals(1, nextStates.size());
        assertTrue(nextStates.contains(PaymentStatus.AUTHORIZED));
    }

    @Test
    @DisplayName("Get valid next states from AUTHORIZED")
    void testGetValidNextStatesFromAuthorized() {
        Set<PaymentStatus> nextStates = stateMachine.getValidNextStates(PaymentStatus.AUTHORIZED);

        assertEquals(1, nextStates.size());
        assertTrue(nextStates.contains(PaymentStatus.CAPTURED));
    }

    // =========================================================================
    // NULL HANDLING TESTS
    // =========================================================================

    @Test
    @DisplayName("Null current status should return false")
    void testNullCurrentStatus() {
        assertFalse(stateMachine.canTransition(null, PaymentStatus.AUTHORIZED));
    }

    @Test
    @DisplayName("Null target status should return false")
    void testNullTargetStatus() {
        assertFalse(stateMachine.canTransition(PaymentStatus.CREATED, null));
    }

    // =========================================================================
    // OUT-OF-ORDER WEBHOOK SCENARIO TEST (Problem #3)
    // =========================================================================

    @Test
    @DisplayName("Scenario: Out-of-order webhooks - CAPTURED arrives before AUTHORIZED")
    void testOutOfOrderWebhookScenario() {
        // Scenario: Due to network issues, CAPTURED webhook arrives first
        payment.setStatus(PaymentStatus.CREATED);

        // Step 1: CAPTURED webhook arrives (skipping AUTHORIZED)
        // This should fail because we can't skip states
        assertThrows(IllegalStateException.class, () -> {
            stateMachine.transition(payment, PaymentStatus.CAPTURED);
        });

        // Correct flow: CREATED -> AUTHORIZED -> CAPTURED
        stateMachine.transition(payment, PaymentStatus.AUTHORIZED);
        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());

        stateMachine.transition(payment, PaymentStatus.CAPTURED);
        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());

        // Step 2: Delayed AUTHORIZED webhook arrives (backward transition)
        // This MUST be rejected to prevent corruption
        assertThrows(IllegalStateException.class, () -> {
            stateMachine.transition(payment, PaymentStatus.AUTHORIZED);
        });

        // Payment should still be CAPTURED (data integrity maintained)
        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
    }

    // =========================================================================
    // RANK-BASED BACKWARD TRANSITION PREVENTION TEST
    // =========================================================================

    @Test
    @DisplayName("Rank-based prevention: Higher rank cannot move to lower rank")
    void testRankBasedBackwardPrevention() {
        // CAPTURED has higher rank than AUTHORIZED
        assertTrue(PaymentStatus.CAPTURED.getRank() > PaymentStatus.AUTHORIZED.getRank());

        // AUTHORIZED has higher rank than CREATED
        assertTrue(PaymentStatus.AUTHORIZED.getRank() > PaymentStatus.CREATED.getRank());

        // Setting payment to CAPTURED
        payment.setStatus(PaymentStatus.CAPTURED);

        // Cannot move back to any lower rank
        assertFalse(stateMachine.canTransition(PaymentStatus.CAPTURED, PaymentStatus.AUTHORIZED));
        assertFalse(stateMachine.canTransition(PaymentStatus.CAPTURED, PaymentStatus.CREATED));
    }
}
