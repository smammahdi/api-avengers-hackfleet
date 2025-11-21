package com.careforall.donation.service;

import com.careforall.donation.dto.CreateDonationRequest;
import com.careforall.donation.dto.DonationResponse;
import com.careforall.donation.entity.Donation;
import com.careforall.donation.entity.DonationStatus;
import com.careforall.donation.event.DonationEvent;
import com.careforall.donation.outbox.OutboxEvent;
import com.careforall.donation.outbox.OutboxEventRepository;
import com.careforall.donation.repository.DonationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Donation Service
 *
 * Manages donation operations with Transactional Outbox pattern for reliable event publishing.
 * Supports guest donations (userId can be null) and CREATED->AUTHORIZED->CAPTURED state flow.
 */
@Service
public class DonationService {

    private static final Logger logger = LoggerFactory.getLogger(DonationService.class);

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Create a new donation with Transactional Outbox pattern
     * Both donation and outbox event are saved in the SAME transaction
     * userId can be null for guest donations
     */
    @Transactional
    public DonationResponse createDonation(CreateDonationRequest request, Long userId) {
        logger.info("Creating donation for campaign {} from user {}", request.getCampaignId(), userId);

        // Step 1: Create and save donation entity with new schema
        Donation donation = Donation.builder()
            .campaignId(request.getCampaignId())
            .userId(userId) // Can be null for guest donations
            .amount(request.getAmount())
            .donorEmail(request.getDonorEmail())
            .donorName(request.getDonorName())
            .paymentMethod(request.getPaymentMethod())
            .message(request.getMessage())
            .isAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false)
            .status(DonationStatus.CREATED)
            .build();

        donation = donationRepository.save(donation);
        logger.info("Donation created with ID: {}", donation.getId());

        // Step 2: Create outbox event in SAME transaction
        DonationEvent event = DonationEvent.created(
            donation.getId().toString(),
            donation.getCampaignId(),
            donation.getUserId(),
            donation.getDonorEmail(),
            donation.getAmount()
        );

        saveOutboxEvent(donation.getId().toString(), "DONATION_CREATED", event);
        logger.info("Donation created and outbox event saved: {}", donation.getId());

        return DonationResponse.fromEntity(donation);
    }

    /**
     * Authorize payment for donation (first step in payment flow)
     * CREATED -> AUTHORIZED
     */
    @Transactional
    public DonationResponse authorizePayment(UUID donationId) {
        logger.info("Authorizing payment for donation: {}", donationId);

        Donation donation = donationRepository.findById(donationId)
            .orElseThrow(() -> new RuntimeException("Donation not found with ID: " + donationId));

        // Authorize the donation (state transition: CREATED -> AUTHORIZED)
        donation.authorize();
        donation = donationRepository.save(donation);

        // Create DONATION_AUTHORIZED event in outbox
        DonationEvent event = DonationEvent.authorized(
            donation.getId().toString(),
            donation.getCampaignId(),
            donation.getUserId(),
            donation.getDonorEmail(),
            donation.getAmount()
        );

        saveOutboxEvent(donation.getId().toString(), "DONATION_AUTHORIZED", event);
        logger.info("Payment authorized for donation: {}", donationId);

        return DonationResponse.fromEntity(donation);
    }

    /**
     * Capture payment for donation (final step - money transferred)
     * AUTHORIZED -> CAPTURED
     */
    @Transactional
    public DonationResponse capturePayment(UUID donationId, String transactionId) {
        logger.info("Capturing payment for donation: {} with transaction {}", donationId, transactionId);

        Donation donation = donationRepository.findById(donationId)
            .orElseThrow(() -> new RuntimeException("Donation not found with ID: " + donationId));

        // Capture the donation (state transition: AUTHORIZED -> CAPTURED)
        donation.capture(transactionId);
        donation = donationRepository.save(donation);

        // Create DONATION_CAPTURED event in outbox
        DonationEvent event = DonationEvent.captured(
            donation.getId().toString(),
            donation.getCampaignId(),
            donation.getUserId(),
            donation.getDonorEmail(),
            donation.getAmount(),
            transactionId
        );

        saveOutboxEvent(donation.getId().toString(), "DONATION_CAPTURED", event);
        logger.info("Payment captured for donation: {}", donationId);

        return DonationResponse.fromEntity(donation);
    }

    /**
     * Link a guest donation to a registered user
     * This is called when a guest who made a donation later registers
     */
    @Transactional
    public void linkDonationToUser(String donorEmail, Long userId) {
        logger.info("Linking donations for email {} to user {}", donorEmail, userId);

        List<Donation> guestDonations = donationRepository.findByDonorEmailOrderByCreatedAtDesc(donorEmail)
            .stream()
            .filter(Donation::isGuestDonation)
            .collect(Collectors.toList());

        for (Donation donation : guestDonations) {
            donation.linkToUser(userId);
            donationRepository.save(donation);
        }

        logger.info("Linked {} guest donations to user {}", guestDonations.size(), userId);
    }

    /**
     * Save outbox event - helper method
     * This is called within the same transaction as the business logic
     */
    private void saveOutboxEvent(String donationId, String eventType, DonationEvent donationEvent) {
        try {
            String payload = objectMapper.writeValueAsString(donationEvent);

            OutboxEvent outboxEvent = OutboxEvent.create(
                donationId,
                "DONATION",
                eventType,
                payload
            );

            outboxEventRepository.save(outboxEvent);
            logger.debug("Outbox event saved: {} for donation {}", eventType, donationId);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize donation event: {}", e.getMessage());
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }

    /**
     * Get donation by ID
     */
    public DonationResponse getDonationById(UUID donationId) {
        Donation donation = donationRepository.findById(donationId)
            .orElseThrow(() -> new RuntimeException("Donation not found with ID: " + donationId));

        return DonationResponse.fromEntity(donation);
    }

    /**
     * Get all donations for a user
     */
    public List<DonationResponse> getUserDonations(Long userId) {
        logger.info("Fetching donations for user: {}", userId);

        return donationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(DonationResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get all donations for a campaign
     */
    public List<DonationResponse> getCampaignDonations(Long campaignId) {
        logger.info("Fetching donations for campaign: {}", campaignId);

        return donationRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId)
            .stream()
            .map(DonationResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get all donations by email (for guest donations)
     */
    public List<DonationResponse> getDonationsByEmail(String donorEmail) {
        logger.info("Fetching donations for email: {}", donorEmail);

        return donationRepository.findByDonorEmailOrderByCreatedAtDesc(donorEmail)
            .stream()
            .map(DonationResponse::fromEntity)
            .collect(Collectors.toList());
    }
}
