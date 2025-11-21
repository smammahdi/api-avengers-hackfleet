package com.careforall.payment.listener;

import com.careforall.payment.dto.PaymentRequest;
import com.careforall.payment.dto.PaymentResponse;
import com.careforall.payment.event.DonationEvent;
import com.careforall.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Donation Event Listener
 *
 * Listens to DONATION_CREATED events from donation-service and processes payments.
 * Automatically triggers payment processing when a new donation is created.
 */
@Component
public class DonationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DonationEventListener.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * Handle DONATION_CREATED events
     *
     * This method is triggered when a donation is created in the donation-service.
     * It processes the payment and publishes PAYMENT_COMPLETED or PAYMENT_FAILED events.
     */
    @RabbitListener(queues = "#{donationQueue.name}")
    public void handleDonationCreated(DonationEvent event) {
        logger.info("Received DONATION_CREATED event for donation: {} (amount: {})",
            event.getDonationId(), event.getAmount());

        try {
            // Build payment request from donation event
            PaymentRequest paymentRequest = buildPaymentRequest(event);

            // Process payment with idempotency and retry logic
            PaymentResponse response = paymentService.processPayment(paymentRequest);

            logger.info("Payment processing completed for donation {}: status={}, fromCache={}",
                event.getDonationId(), response.getStatus(), response.isFromCache());

            // The PaymentService will automatically publish PAYMENT_COMPLETED or PAYMENT_FAILED events

        } catch (Exception e) {
            logger.error("Error processing payment for donation {}: {}",
                event.getDonationId(), e.getMessage(), e);
            // Depending on your error handling strategy, you might want to:
            // 1. Retry the message (let RabbitMQ requeue)
            // 2. Send to a dead-letter queue
            // 3. Publish a PAYMENT_FAILED event manually
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    /**
     * Build PaymentRequest from DonationEvent
     */
    private PaymentRequest buildPaymentRequest(DonationEvent event) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("campaign_id", String.valueOf(event.getCampaignId()));
        metadata.put("event_timestamp", event.getTimestamp().toString());
        metadata.put("source", "donation-event");

        return PaymentRequest.builder()
            .idempotencyKey(event.getIdempotencyKey())
            .donationId(event.getDonationId())
            .userId(event.getUserId())
            .amount(event.getAmount())
            .paymentMethod(event.getPaymentMethod() != null ? event.getPaymentMethod() : "credit_card")
            .metadata(metadata)
            .build();
    }
}
