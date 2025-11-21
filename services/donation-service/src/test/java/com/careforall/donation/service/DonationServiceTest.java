package com.careforall.donation.service;

import com.careforall.donation.dto.CreateDonationRequest;
import com.careforall.donation.dto.DonationResponse;
import com.careforall.donation.entity.Donation;
import com.careforall.donation.entity.DonationStatus;
import com.careforall.donation.outbox.OutboxEvent;
import com.careforall.donation.outbox.OutboxEventRepository;
import com.careforall.donation.repository.DonationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Donation Service Tests
 *
 * Unit tests for DonationService with focus on Transactional Outbox pattern.
 */
@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DonationService donationService;

    private CreateDonationRequest createRequest;
    private Donation donation;
    private UUID testDonationId;

    @BeforeEach
    void setUp() {
        testDonationId = UUID.randomUUID();

        createRequest = new CreateDonationRequest();
        createRequest.setCampaignId(1L);
        createRequest.setAmount(new BigDecimal("100.00"));
        createRequest.setDonorName("John Doe");
        createRequest.setDonorEmail("john@example.com");
        createRequest.setPaymentMethod("CREDIT_CARD");
        createRequest.setIsAnonymous(false);

        donation = new Donation();
        donation.setId(testDonationId);
        donation.setCampaignId(1L);
        donation.setUserId(1L);
        donation.setAmount(new BigDecimal("100.00"));
        donation.setDonorName("John Doe");
        donation.setDonorEmail("john@example.com");
        donation.setPaymentMethod("CREDIT_CARD");
        donation.setStatus(DonationStatus.CREATED);
        donation.setIsAnonymous(false);
    }

    @Test
    void testCreateDonation_Success() throws Exception {
        // Arrange
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(new OutboxEvent());

        // Act
        DonationResponse response = donationService.createDonation(createRequest, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(testDonationId, response.getId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());

        // Verify outbox event was saved (Transactional Outbox pattern)
        verify(outboxEventRepository, atLeastOnce()).save(any(OutboxEvent.class));
    }

    @Test
    void testGetDonationById_Success() {
        // Arrange
        when(donationRepository.findById(testDonationId)).thenReturn(Optional.of(donation));

        // Act
        DonationResponse response = donationService.getDonationById(testDonationId);

        // Assert
        assertNotNull(response);
        assertEquals(testDonationId, response.getId());
        assertEquals("John Doe", response.getDonorName());
    }

    @Test
    void testGetDonationById_NotFound() {
        // Arrange
        when(donationRepository.findById(testDonationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> donationService.getDonationById(testDonationId));
    }

    @Test
    void testGetUserDonations() {
        // Arrange
        List<Donation> donations = Arrays.asList(donation);
        when(donationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(donations);

        // Act
        List<DonationResponse> responses = donationService.getUserDonations(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("John Doe", responses.get(0).getDonorName());
    }

    @Test
    void testGetCampaignDonations() {
        // Arrange
        List<Donation> donations = Arrays.asList(donation);
        when(donationRepository.findByCampaignIdOrderByCreatedAtDesc(1L)).thenReturn(donations);

        // Act
        List<DonationResponse> responses = donationService.getCampaignDonations(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void testGetDonationsByEmail() {
        // Arrange
        List<Donation> donations = Arrays.asList(donation);
        when(donationRepository.findByDonorEmailOrderByCreatedAtDesc("john@example.com")).thenReturn(donations);

        // Act
        List<DonationResponse> responses = donationService.getDonationsByEmail("john@example.com");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("john@example.com", responses.get(0).getDonorEmail());
    }
}
