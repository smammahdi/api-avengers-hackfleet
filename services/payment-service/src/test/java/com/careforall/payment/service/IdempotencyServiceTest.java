package com.careforall.payment.service;

import com.careforall.payment.entity.Payment;
import com.careforall.payment.enums.PaymentStatus;
import com.careforall.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Idempotency Service Tests
 *
 * Tests idempotent payment handling.
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    private Payment validPayment;
    private String idempotencyKey;

    @BeforeEach
    void setUp() {
        idempotencyKey = "test-idempotency-key-001";
        validPayment = Payment.builder()
            .id(1L)
            .paymentId("PAY-001")
            .idempotencyKey(idempotencyKey)
            .donationId(100L)
            .userId(200L)
            .amount(new BigDecimal("100.00"))
            .status(PaymentStatus.COMPLETED)
            .build();
        validPayment.setCreatedAt(LocalDateTime.now());
        validPayment.setUpdatedAt(LocalDateTime.now());
        validPayment.setIdempotencyExpiresAt(LocalDateTime.now().plusHours(24));
    }

    @Test
    void testFindExistingPaymentWithinWindow() {
        when(paymentRepository.findValidPaymentByIdempotencyKey(eq(idempotencyKey), any(LocalDateTime.class)))
            .thenReturn(Optional.of(validPayment));

        Optional<Payment> result = idempotencyService.findExistingPayment(idempotencyKey);

        assertTrue(result.isPresent());
        assertEquals(validPayment.getPaymentId(), result.get().getPaymentId());
    }

    @Test
    void testFindExistingPaymentNotFound() {
        when(paymentRepository.findValidPaymentByIdempotencyKey(eq(idempotencyKey), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());
        when(paymentRepository.findByIdempotencyKey(idempotencyKey))
            .thenReturn(Optional.empty());

        Optional<Payment> result = idempotencyService.findExistingPayment(idempotencyKey);

        assertFalse(result.isPresent());
    }

    @Test
    void testValidIdempotencyKey() {
        assertTrue(idempotencyService.isValidIdempotencyKey("valid-key"));
        assertTrue(idempotencyService.isValidIdempotencyKey("123-456-789"));
    }

    @Test
    void testInvalidIdempotencyKeyNull() {
        assertFalse(idempotencyService.isValidIdempotencyKey(null));
    }

    @Test
    void testInvalidIdempotencyKeyEmpty() {
        assertFalse(idempotencyService.isValidIdempotencyKey(""));
        assertFalse(idempotencyService.isValidIdempotencyKey("   "));
    }

    @Test
    void testInvalidIdempotencyKeyTooLong() {
        String longKey = "a".repeat(256);
        assertFalse(idempotencyService.isValidIdempotencyKey(longKey));
    }

    @Test
    void testCheckIdempotencyValid() {
        when(paymentRepository.findValidPaymentByIdempotencyKey(eq(idempotencyKey), any(LocalDateTime.class)))
            .thenReturn(Optional.of(validPayment));

        Optional<Payment> result = idempotencyService.checkIdempotency(idempotencyKey);

        assertTrue(result.isPresent());
    }

    @Test
    void testCheckIdempotencyInvalidKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            idempotencyService.checkIdempotency(null);
        });
    }

    @Test
    void testIsWithinIdempotencyWindow() {
        assertTrue(idempotencyService.isWithinIdempotencyWindow(validPayment));

        // Test expired payment
        validPayment.setIdempotencyExpiresAt(LocalDateTime.now().minusHours(1));
        assertFalse(idempotencyService.isWithinIdempotencyWindow(validPayment));
    }

    @Test
    void testCalculateIdempotencyExpiration() {
        LocalDateTime expiration = idempotencyService.calculateIdempotencyExpiration();
        assertNotNull(expiration);
        assertTrue(expiration.isAfter(LocalDateTime.now()));
    }
}
