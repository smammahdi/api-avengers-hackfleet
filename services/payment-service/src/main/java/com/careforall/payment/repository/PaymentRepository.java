package com.careforall.payment.repository;

import com.careforall.payment.entity.Payment;
import com.careforall.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 *
 * Data access layer for Payment entities with idempotency support.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by idempotency key
     * Used for idempotency checks
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find payment by payment ID
     */
    Optional<Payment> findByPaymentId(String paymentId);

    /**
     * Find payment by donation ID
     */
    Optional<Payment> findByDonationId(Long donationId);

    /**
     * Find all payments by donation ID
     */
    List<Payment> findAllByDonationId(Long donationId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find payments by user ID
     */
    List<Payment> findByUserId(Long userId);

    /**
     * Check if payment exists with idempotency key and is not expired
     */
    @Query("SELECT p FROM Payment p WHERE p.idempotencyKey = :idempotencyKey " +
           "AND p.idempotencyExpiresAt > :now")
    Optional<Payment> findValidPaymentByIdempotencyKey(
        @Param("idempotencyKey") String idempotencyKey,
        @Param("now") LocalDateTime now
    );

    /**
     * Find expired idempotency records for cleanup
     */
    @Query("SELECT p FROM Payment p WHERE p.idempotencyExpiresAt < :now")
    List<Payment> findExpiredIdempotencyRecords(@Param("now") LocalDateTime now);

    /**
     * Find pending or processing payments older than a certain time
     * Useful for identifying stuck payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PROCESSING') " +
           "AND p.createdAt < :threshold")
    List<Payment> findStuckPayments(@Param("threshold") LocalDateTime threshold);

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);

    /**
     * Check if a payment exists for a donation
     */
    boolean existsByDonationId(Long donationId);
}
