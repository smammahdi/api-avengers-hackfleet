package com.careforall.banking.service;

import com.careforall.banking.dto.PaymentAuthorizationRequest;
import com.careforall.banking.dto.PaymentCaptureRequest;
import com.careforall.banking.entity.BankAccount;
import com.careforall.banking.entity.BankTransaction;
import com.careforall.banking.enums.TransactionType;
import com.careforall.banking.event.BankingEvent;
import com.careforall.banking.repository.BankAccountRepository;
import com.careforall.banking.repository.BankTransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Banking Service
 *
 * Handles payment authorization and capture with:
 * - Balance validation
 * - Fund locking/releasing
 * - Transaction audit trail
 * - Resilience4j fault tolerance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankingService {

    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Authorize payment: Check balance and lock funds
     *
     * @param request Payment authorization request
     * @return Banking event (PAYMENT_AUTHORIZED or PAYMENT_FAILED)
     */
    @Transactional
    @CircuitBreaker(name = "bankingService", fallbackMethod = "authorizeFallback")
    @Retry(name = "bankingService")
    public BankingEvent authorizePayment(PaymentAuthorizationRequest request) {
        log.info("Processing authorization request - PaymentID: {}, Email: {}, Amount: {}",
                request.getPaymentId(), request.getDonorEmail(), request.getAmount());

        try {
            // Check for duplicate authorization (idempotency)
            Optional<BankTransaction> existingTxn = transactionRepository.findByExternalReference(request.getPaymentId());
            if (existingTxn.isPresent() && existingTxn.get().getTransactionType() == TransactionType.AUTHORIZATION) {
                log.info("Duplicate authorization request detected for PaymentID: {}", request.getPaymentId());
                return buildSuccessEvent("PAYMENT_AUTHORIZED", request.getPaymentId(),
                        request.getAmount(), existingTxn.get().getId().toString());
            }

            // Find account by email (supports guest donations)
            BankAccount account = accountRepository.findByEmail(request.getDonorEmail())
                    .orElseThrow(() -> new RuntimeException("Account not found for email: " + request.getDonorEmail()));

            // Check if sufficient balance
            if (account.getAvailableBalance().compareTo(request.getAmount()) < 0) {
                log.warn("Insufficient balance - PaymentID: {}, Available: {}, Required: {}",
                        request.getPaymentId(), account.getAvailableBalance(), request.getAmount());

                return buildFailureEvent("PAYMENT_FAILED", request.getPaymentId(),
                        request.getAmount(), "Insufficient balance");
            }

            // Lock funds
            BigDecimal balanceBefore = account.getTotalBalance();
            account.lockFunds(request.getAmount());
            accountRepository.save(account);

            // Create transaction record
            BankTransaction transaction = BankTransaction.builder()
                    .accountId(account.getId())
                    .externalReference(request.getPaymentId())
                    .transactionType(TransactionType.AUTHORIZATION)
                    .amount(request.getAmount())
                    .balanceBefore(balanceBefore)
                    .balanceAfter(account.getTotalBalance())
                    .description("Funds locked for donation payment")
                    .build();
            transactionRepository.save(transaction);

            log.info("✅ Payment authorized - PaymentID: {}, TransactionID: {}",
                    request.getPaymentId(), transaction.getId());

            return buildSuccessEvent("PAYMENT_AUTHORIZED", request.getPaymentId(),
                    request.getAmount(), transaction.getId().toString());

        } catch (Exception e) {
            log.error("❌ Authorization failed - PaymentID: {}, Error: {}",
                    request.getPaymentId(), e.getMessage());

            return buildFailureEvent("PAYMENT_FAILED", request.getPaymentId(),
                    request.getAmount(), e.getMessage());
        }
    }

    /**
     * Capture payment: Transfer locked funds
     *
     * @param request Payment capture request
     * @return Banking event (PAYMENT_CAPTURED or PAYMENT_FAILED)
     */
    @Transactional
    @CircuitBreaker(name = "bankingService", fallbackMethod = "captureFallback")
    @Retry(name = "bankingService")
    public BankingEvent capturePayment(PaymentCaptureRequest request) {
        log.info("Processing capture request - PaymentID: {}, Email: {}, Amount: {}",
                request.getPaymentId(), request.getDonorEmail(), request.getAmount());

        try {
            // Check for duplicate capture (idempotency)
            Optional<BankTransaction> existingCapture = transactionRepository
                    .findByExternalReferenceOrderByCreatedAtDesc(request.getPaymentId())
                    .stream()
                    .filter(txn -> txn.getTransactionType() == TransactionType.CAPTURE)
                    .findFirst();

            if (existingCapture.isPresent()) {
                log.info("Duplicate capture request detected for PaymentID: {}", request.getPaymentId());
                return buildSuccessEvent("PAYMENT_CAPTURED", request.getPaymentId(),
                        request.getAmount(), existingCapture.get().getId().toString());
            }

            // Find account
            BankAccount account = accountRepository.findByEmail(request.getDonorEmail())
                    .orElseThrow(() -> new RuntimeException("Account not found for email: " + request.getDonorEmail()));

            // Capture locked funds
            BigDecimal balanceBefore = account.getTotalBalance();
            account.captureFunds(request.getAmount());
            accountRepository.save(account);

            // Create transaction record
            BankTransaction transaction = BankTransaction.builder()
                    .accountId(account.getId())
                    .externalReference(request.getPaymentId())
                    .transactionType(TransactionType.CAPTURE)
                    .amount(request.getAmount())
                    .balanceBefore(balanceBefore)
                    .balanceAfter(account.getTotalBalance())
                    .description("Funds captured and transferred to charity")
                    .build();
            transactionRepository.save(transaction);

            log.info("✅ Payment captured - PaymentID: {}, TransactionID: {}",
                    request.getPaymentId(), transaction.getId());

            return buildSuccessEvent("PAYMENT_CAPTURED", request.getPaymentId(),
                    request.getAmount(), transaction.getId().toString());

        } catch (Exception e) {
            log.error("❌ Capture failed - PaymentID: {}, Error: {}",
                    request.getPaymentId(), e.getMessage());

            return buildFailureEvent("PAYMENT_FAILED", request.getPaymentId(),
                    request.getAmount(), e.getMessage());
        }
    }

    /**
     * Fallback method for authorization failures
     */
    private BankingEvent authorizeFallback(PaymentAuthorizationRequest request, Throwable throwable) {
        log.error("Circuit breaker fallback triggered for authorization - PaymentID: {}, Error: {}",
                request.getPaymentId(), throwable.getMessage());

        return buildFailureEvent("PAYMENT_FAILED", request.getPaymentId(),
                request.getAmount(), "Service temporarily unavailable. Please try again later.");
    }

    /**
     * Fallback method for capture failures
     */
    private BankingEvent captureFallback(PaymentCaptureRequest request, Throwable throwable) {
        log.error("Circuit breaker fallback triggered for capture - PaymentID: {}, Error: {}",
                request.getPaymentId(), throwable.getMessage());

        return buildFailureEvent("PAYMENT_FAILED", request.getPaymentId(),
                request.getAmount(), "Service temporarily unavailable. Please try again later.");
    }

    /**
     * Build success event
     */
    private BankingEvent buildSuccessEvent(String eventType, String paymentId,
                                          BigDecimal amount, String transactionId) {
        return BankingEvent.builder()
                .eventType(eventType)
                .paymentId(paymentId)
                .transactionId(transactionId)
                .amount(amount)
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Build failure event
     */
    private BankingEvent buildFailureEvent(String eventType, String paymentId,
                                          BigDecimal amount, String failureReason) {
        return BankingEvent.builder()
                .eventType(eventType)
                .paymentId(paymentId)
                .amount(amount)
                .status("FAILED")
                .failureReason(failureReason)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Add funds to account (for testing/admin purposes)
     */
    @Transactional
    public void addFunds(String email, BigDecimal amount) {
        BankAccount account = accountRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Create account if doesn't exist
                    return BankAccount.builder()
                            .email(email)
                            .accountHolderName("User")
                            .availableBalance(BigDecimal.ZERO)
                            .lockedBalance(BigDecimal.ZERO)
                            .build();
                });

        account.addFunds(amount);
        accountRepository.save(account);

        log.info("Added {} to account: {}", amount, email);
    }
}
