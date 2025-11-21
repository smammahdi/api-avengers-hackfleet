package com.careforall.banking.listener;

import com.careforall.banking.dto.PaymentAuthorizationRequest;
import com.careforall.banking.dto.PaymentCaptureRequest;
import com.careforall.banking.event.BankingEvent;
import com.careforall.banking.service.BankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Payment Event Listener
 *
 * Listens to payment requests from Payment Service and processes them
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final BankingService bankingService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Listen for payment authorization and capture requests from Payment Service
     *
     * This listener processes both authorization and capture requests since
     * the Banking Service auto-captures after successful authorization
     */
    @RabbitListener(queues = "banking.request.queue")
    public void handlePaymentRequest(Object request) {
        log.info("📨 Received payment request: {}", request.getClass().getSimpleName());

        try {
            BankingEvent event;

            // Process based on request type
            if (request instanceof PaymentAuthorizationRequest) {
                PaymentAuthorizationRequest authRequest = (PaymentAuthorizationRequest) request;
                log.info("Processing authorization - PaymentID: {}, Amount: {}",
                        authRequest.getPaymentId(), authRequest.getAmount());

                // Process authorization and auto-capture
                event = bankingService.authorizePayment(authRequest);

                // If authorization succeeded, immediately capture
                if (event.getStatus().equals("SUCCESS")) {
                    PaymentCaptureRequest captureRequest = PaymentCaptureRequest.builder()
                            .paymentId(authRequest.getPaymentId())
                            .donorEmail(authRequest.getDonorEmail())
                            .userId(authRequest.getUserId())
                            .amount(authRequest.getAmount())
                            .build();

                    event = bankingService.capturePayment(captureRequest);
                }

            } else if (request instanceof PaymentCaptureRequest) {
                PaymentCaptureRequest captureRequest = (PaymentCaptureRequest) request;
                log.info("Processing capture - PaymentID: {}, Amount: {}",
                        captureRequest.getPaymentId(), captureRequest.getAmount());

                event = bankingService.capturePayment(captureRequest);

            } else {
                log.warn("Unknown request type: {}", request.getClass().getName());
                return;
            }

            // Publish result back to Payment Service using banking.response routing key
            rabbitTemplate.convertAndSend("banking.exchange", "banking.response", event);

            log.info("✅ Published {} event for PaymentID: {}", event.getEventType(), event.getPaymentId());

        } catch (Exception e) {
            log.error("❌ Error processing payment request: {}", e.getMessage(), e);
        }
    }
}
