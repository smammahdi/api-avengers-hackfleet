package com.careforall.payment.service;

import com.careforall.payment.dto.PaymentAuthorizationRequest;
import com.careforall.payment.dto.PaymentRequest;
import com.careforall.payment.dto.PaymentResponse;
import com.careforall.payment.entity.Payment;
import com.careforall.payment.enums.PaymentStatus;
import com.careforall.payment.event.PaymentEvent;
import com.careforall.payment.repository.PaymentRepository;
import com.careforall.payment.statemachine.PaymentStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Service
 *
 * Enhanced payment processing with:
 * - Idempotency support (24-hour window)
 * - State machine for payment status transitions
 * - Integration with Banking Service via RabbitMQ
 * - Event publishing for donation payment lifecycle
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    // Payment events (to donation-service)
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    // Banking service communication
    public static final String BANKING_EXCHANGE = "banking.exchange";
    public static final String BANKING_REQUEST_ROUTING_KEY = "banking.authorize";

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private PaymentStateMachine stateMachine;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Process payment with idempotency and Banking Service integration
     *
     * Flow: Creates payment → Sends authorization request to Banking Service →
     * BankingEventListener handles response → Updates donation-service
     *
     * @param request Payment request
     * @return Payment response
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment for donation: {} with idempotency key: {}",
            request.getDonationId(), request.getIdempotencyKey());

        // Step 1: Check idempotency - return existing result if found
        Optional<Payment> existingPayment = idempotencyService.checkIdempotency(request.getIdempotencyKey());
        if (existingPayment.isPresent()) {
            logger.info("Idempotent request detected - returning existing payment result");
            return toResponseWithCache(existingPayment.get());
        }

        // Step 2: Create new payment in CREATED state
        Payment payment = createPayment(request);
        payment = paymentRepository.save(payment);

        // Step 3: Send authorization request to Banking Service
        sendAuthorizationRequestToBankingService(payment, request);

        // Step 4: Return response with CREATED status
        // Note: Actual authorization/capture happens asynchronously via Banking Service
        // BankingEventListener will handle the response and update status
        return toResponse(payment);
    }

    /**
     * Create payment entity from request
     */
    private Payment createPayment(PaymentRequest request) {
        String paymentId = "PAY-" + UUID.randomUUID().toString();

        Payment payment = Payment.builder()
            .paymentId(paymentId)
            .idempotencyKey(request.getIdempotencyKey())
            .donationId(request.getDonationId())
            .userId(request.getUserId())
            .amount(request.getAmount())
            .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "credit_card")
            .status(PaymentStatus.CREATED)
            .attemptCount(0)
            .build();

        // Add metadata if provided
        if (request.getMetadata() != null) {
            request.getMetadata().forEach(payment::addMetadata);
        }

        payment.addMetadata("created_by", "donation-service");
        payment.addMetadata("payment_gateway", "mock-gateway");

        logger.debug("Created payment entity: {}", paymentId);
        return payment;
    }

    /**
     * Send authorization request to Banking Service via RabbitMQ
     *
     * Banking Service will:
     * 1. Check user balance
     * 2. Lock funds if sufficient balance
     * 3. Send back PAYMENT_AUTHORIZED or PAYMENT_FAILED event
     * 4. Auto-capture after authorization
     */
    private void sendAuthorizationRequestToBankingService(Payment payment, PaymentRequest request) {
        logger.info("Sending authorization request to Banking Service for payment: {}",
            payment.getPaymentId());

        try {
            // Build authorization request
            PaymentAuthorizationRequest authRequest = PaymentAuthorizationRequest.builder()
                .paymentId(payment.getPaymentId())
                .donorEmail(request.getPaymentMethod()) // Using payment method as email for now
                .userId(request.getUserId())
                .amount(request.getAmount())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

            // Send to Banking Service via RabbitMQ
            rabbitTemplate.convertAndSend(
                BANKING_EXCHANGE,
                BANKING_REQUEST_ROUTING_KEY,
                authRequest
            );

            logger.info("Authorization request sent to Banking Service for payment: {}",
                payment.getPaymentId());

            // Add metadata
            payment.addMetadata("authorization_sent_at", LocalDateTime.now().toString());
            payment.addMetadata("banking_service_request", "AUTHORIZE");
            paymentRepository.save(payment);

        } catch (Exception e) {
            logger.error("Failed to send authorization request to Banking Service for payment {}: {}",
                payment.getPaymentId(), e.getMessage(), e);

            // Mark payment as failed if we can't even send the request
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("Failed to communicate with Banking Service: " + e.getMessage());
            paymentRepository.save(payment);

            // Publish failure event to donation-service
            publishPaymentEvent(payment);
        }
    }

    /**
     * Publish payment event to RabbitMQ
     */
    private void publishPaymentEvent(Payment payment) {
        PaymentEvent event = new PaymentEvent(
            payment.getPaymentId(),
            payment.getDonationId(),
            payment.getUserId(),
            payment.getAmount(),
            payment.getStatus().name(),
            LocalDateTime.now()
        );

        String routingKey = payment.getStatus() == PaymentStatus.CAPTURED
            ? PAYMENT_COMPLETED_ROUTING_KEY
            : PAYMENT_FAILED_ROUTING_KEY;

        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, routingKey, event);

        logger.info("Published payment event: {} with status {} to routing key: {}",
            payment.getPaymentId(), payment.getStatus(), routingKey);
    }

    /**
     * Convert Payment entity to PaymentResponse
     */
    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
            .paymentId(payment.getPaymentId())
            .idempotencyKey(payment.getIdempotencyKey())
            .donationId(payment.getDonationId())
            .userId(payment.getUserId())
            .amount(payment.getAmount())
            .paymentMethod(payment.getPaymentMethod())
            .status(payment.getStatus())
            .message(payment.getStatus() == PaymentStatus.CAPTURED
                ? "Payment processed successfully"
                : payment.getStatus() == PaymentStatus.AUTHORIZED
                ? "Payment authorized, pending capture"
                : "Payment created: " + (payment.getErrorMessage() != null ? payment.getErrorMessage() : "Processing"))
            .metadata(payment.getMetadata())
            .attemptCount(payment.getAttemptCount())
            .errorMessage(payment.getErrorMessage())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .fromCache(false)
            .build();
    }

    /**
     * Convert Payment entity to PaymentResponse with cache flag
     */
    private PaymentResponse toResponseWithCache(Payment payment) {
        PaymentResponse response = toResponse(payment);
        response.setFromCache(true);
        response.setMessage("Idempotent request - returning cached result");
        return response;
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentById(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
            .map(this::toResponse);
    }

    /**
     * Get payment by donation ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByDonationId(Long donationId) {
        return paymentRepository.findByDonationId(donationId)
            .map(this::toResponse);
    }
}
