package com.careforall.payment.listener;

import com.careforall.payment.entity.Payment;
import com.careforall.payment.enums.PaymentStatus;
import com.careforall.payment.event.BankingEvent;
import com.careforall.payment.event.PaymentEvent;
import com.careforall.payment.repository.PaymentRepository;
import com.careforall.payment.statemachine.PaymentStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Banking Event Listener
 *
 * Listens to events from Banking Service and updates payment status accordingly.
 * Handles: PAYMENT_AUTHORIZED, PAYMENT_CAPTURED, PAYMENT_FAILED
 */
@Component
public class BankingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BankingEventListener.class);

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentStateMachine stateMachine;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Listen to banking response queue for authorization/capture/failure events
     */
    @RabbitListener(queues = "banking.response.queue")
    @Transactional
    public void handleBankingEvent(BankingEvent event) {
        logger.info("Received banking event: {} for payment: {}",
            event.getEventType(), event.getPaymentId());

        try {
            // Find the payment
            Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(event.getPaymentId());

            if (paymentOpt.isEmpty()) {
                logger.error("Payment not found: {}", event.getPaymentId());
                return;
            }

            Payment payment = paymentOpt.get();

            // Handle event based on type
            switch (event.getEventType()) {
                case "PAYMENT_AUTHORIZED":
                    handlePaymentAuthorized(payment, event);
                    break;

                case "PAYMENT_CAPTURED":
                    handlePaymentCaptured(payment, event);
                    break;

                case "PAYMENT_FAILED":
                    handlePaymentFailed(payment, event);
                    break;

                default:
                    logger.warn("Unknown event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            logger.error("Error processing banking event for payment {}: {}",
                event.getPaymentId(), e.getMessage(), e);
        }
    }

    /**
     * Handle payment authorization success from Banking Service
     */
    private void handlePaymentAuthorized(Payment payment, BankingEvent event) {
        logger.info("Processing PAYMENT_AUTHORIZED for payment: {}", payment.getPaymentId());

        try {
            // Transition to AUTHORIZED state using state machine
            stateMachine.transitionToAuthorized(payment);

            // Add banking transaction metadata
            payment.addMetadata("banking_transaction_id", event.getTransactionId());
            payment.addMetadata("authorization_time", event.getTimestamp().toString());
            payment.addMetadata("banking_status", event.getStatus());

            paymentRepository.save(payment);

            logger.info("Payment {} successfully authorized with transaction ID: {}",
                payment.getPaymentId(), event.getTransactionId());

            // Note: We don't publish to donation-service yet - waiting for CAPTURED

        } catch (Exception e) {
            logger.error("Failed to process authorization for payment {}: {}",
                payment.getPaymentId(), e.getMessage(), e);

            // Mark as failed and publish failure event
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Failed to process authorization: " + e.getMessage());
            paymentRepository.save(payment);
            publishPaymentFailedEvent(payment);
        }
    }

    /**
     * Handle payment capture success from Banking Service
     */
    private void handlePaymentCaptured(Payment payment, BankingEvent event) {
        logger.info("Processing PAYMENT_CAPTURED for payment: {}", payment.getPaymentId());

        try {
            // Transition to CAPTURED state using state machine
            stateMachine.transitionToCaptured(payment);

            // Update metadata with capture details
            payment.addMetadata("capture_time", event.getTimestamp().toString());
            payment.addMetadata("final_amount", event.getAmount().toString());
            payment.addMetadata("completion_time", LocalDateTime.now().toString());

            paymentRepository.save(payment);

            logger.info("Payment {} successfully captured for amount: {}",
                payment.getPaymentId(), event.getAmount());

            // Publish PAYMENT_COMPLETED event to donation-service
            publishPaymentCompletedEvent(payment);

        } catch (Exception e) {
            logger.error("Failed to process capture for payment {}: {}",
                payment.getPaymentId(), e.getMessage(), e);

            // Mark as failed and publish failure event
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Failed to process capture: " + e.getMessage());
            paymentRepository.save(payment);
            publishPaymentFailedEvent(payment);
        }
    }

    /**
     * Handle payment failure from Banking Service
     */
    private void handlePaymentFailed(Payment payment, BankingEvent event) {
        logger.warn("Processing PAYMENT_FAILED for payment: {} - Reason: {}",
            payment.getPaymentId(), event.getFailureReason());

        try {
            // Update payment status to FAILED
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(event.getFailureReason());

            // Add failure metadata
            payment.addMetadata("failed_at", event.getTimestamp().toString());
            payment.addMetadata("failure_reason", event.getFailureReason());
            if (event.getTransactionId() != null) {
                payment.addMetadata("banking_transaction_id", event.getTransactionId());
            }

            paymentRepository.save(payment);

            logger.info("Payment {} marked as failed: {}",
                payment.getPaymentId(), event.getFailureReason());

            // Publish PAYMENT_FAILED event to donation-service
            publishPaymentFailedEvent(payment);

        } catch (Exception e) {
            logger.error("Failed to process payment failure for payment {}: {}",
                payment.getPaymentId(), e.getMessage(), e);
        }
    }

    /**
     * Publish payment completed event to donation-service
     */
    private void publishPaymentCompletedEvent(Payment payment) {
        PaymentEvent event = new PaymentEvent(
            payment.getPaymentId(),
            payment.getDonationId(),
            payment.getUserId(),
            payment.getAmount(),
            PaymentStatus.CAPTURED.name(),
            LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_COMPLETED_ROUTING_KEY, event);

        logger.info("Published PAYMENT_COMPLETED event for payment: {} to donation-service",
            payment.getPaymentId());
    }

    /**
     * Publish payment failed event to donation-service
     */
    private void publishPaymentFailedEvent(Payment payment) {
        PaymentEvent event = new PaymentEvent(
            payment.getPaymentId(),
            payment.getDonationId(),
            payment.getUserId(),
            payment.getAmount(),
            PaymentStatus.FAILED.name(),
            LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_FAILED_ROUTING_KEY, event);

        logger.info("Published PAYMENT_FAILED event for payment: {} to donation-service",
            payment.getPaymentId());
    }
}
