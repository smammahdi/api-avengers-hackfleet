package com.careforall.payment.service;

import com.careforall.payment.entity.Payment;
import com.careforall.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Idempotency Service
 *
 * Handles idempotent payment processing to prevent duplicate payments.
 * Uses idempotency keys with a 24-hour window to detect and handle duplicate requests.
 *
 * Key Features:
 * - Checks for existing payments with the same idempotency key
 * - Returns existing results within the 24-hour window
 * - Allows new processing after the idempotency window expires
 * - Thread-safe with database-level uniqueness constraints
 */
@Service
public class IdempotencyService {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);
    private static final int IDEMPOTENCY_WINDOW_HOURS = 24;

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Check if a payment with this idempotency key already exists and is valid
     *
     * @param idempotencyKey The idempotency key to check
     * @return Optional containing the existing payment if found and valid
     */
    @Transactional(readOnly = true)
    public Optional<Payment> findExistingPayment(String idempotencyKey) {
        logger.debug("Checking for existing payment with idempotency key: {}", idempotencyKey);

        // Find payment with this idempotency key that hasn't expired
        Optional<Payment> existingPayment = paymentRepository
            .findValidPaymentByIdempotencyKey(idempotencyKey, LocalDateTime.now());

        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            logger.info("Found existing payment {} with idempotency key {} (status: {})",
                payment.getPaymentId(), idempotencyKey, payment.getStatus());
            return existingPayment;
        }

        // Also check for any payment with this key (including expired)
        Optional<Payment> anyPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (anyPayment.isPresent() && anyPayment.get().isIdempotencyExpired()) {
            logger.warn("Found expired idempotency record for key: {}. Allowing new payment.", idempotencyKey);
        }

        return Optional.empty();
    }

    /**
     * Validate idempotency key format
     *
     * @param idempotencyKey The key to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            logger.warn("Invalid idempotency key: null or empty");
            return false;
        }

        if (idempotencyKey.length() > 255) {
            logger.warn("Invalid idempotency key: too long (max 255 characters)");
            return false;
        }

        return true;
    }

    /**
     * Check if we should process this payment or return existing result
     *
     * @param idempotencyKey The idempotency key
     * @return Optional containing existing payment if duplicate detected, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<Payment> checkIdempotency(String idempotencyKey) {
        if (!isValidIdempotencyKey(idempotencyKey)) {
            throw new IllegalArgumentException("Invalid idempotency key: " + idempotencyKey);
        }

        return findExistingPayment(idempotencyKey);
    }

    /**
     * Clean up expired idempotency records
     * This can be called periodically to maintain database cleanliness
     *
     * @return Number of records cleaned up
     */
    @Transactional
    public int cleanupExpiredRecords() {
        logger.info("Starting cleanup of expired idempotency records");

        var expiredRecords = paymentRepository
            .findExpiredIdempotencyRecords(LocalDateTime.now());

        int count = expiredRecords.size();
        logger.info("Found {} expired idempotency records (older than {} hours)",
            count, IDEMPOTENCY_WINDOW_HOURS);

        // Note: We typically don't delete payment records, but we could update them
        // or move them to an archive table. For now, we just log them.

        return count;
    }

    /**
     * Generate idempotency expiration time
     *
     * @return LocalDateTime for when the idempotency window expires
     */
    public LocalDateTime calculateIdempotencyExpiration() {
        return LocalDateTime.now().plusHours(IDEMPOTENCY_WINDOW_HOURS);
    }

    /**
     * Check if a payment is within the idempotency window
     *
     * @param payment The payment to check
     * @return true if within window, false otherwise
     */
    public boolean isWithinIdempotencyWindow(Payment payment) {
        return !payment.isIdempotencyExpired();
    }
}
