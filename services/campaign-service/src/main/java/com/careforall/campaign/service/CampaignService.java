package com.careforall.campaign.service;

import com.careforall.campaign.config.RabbitMQConfig;
import com.careforall.campaign.dto.CampaignRequest;
import com.careforall.campaign.dto.CampaignResponse;
import com.careforall.campaign.entity.Campaign;
import com.careforall.campaign.entity.CampaignStatus;
import com.careforall.campaign.event.CampaignEvent;
import com.careforall.campaign.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Campaign Service
 *
 * Business logic for campaign management.
 */
@Service
public class CampaignService {

    private static final Logger logger = LoggerFactory.getLogger(CampaignService.class);

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Get all active campaigns
     */
    public List<CampaignResponse> getAllCampaigns() {
        logger.info("Fetching all active campaigns");
        return campaignRepository.findByStatus(CampaignStatus.ACTIVE)
            .stream()
            .map(CampaignResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get campaign by ID
     */
    public CampaignResponse getCampaignById(Long id) {
        logger.info("Fetching campaign with ID: {}", id);
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        return CampaignResponse.fromEntity(campaign);
    }

    /**
     * Get campaigns by category
     */
    public List<CampaignResponse> getCampaignsByCategory(String category) {
        logger.info("Fetching campaigns in category: {}", category);
        return campaignRepository.findByCategoryAndStatus(category, CampaignStatus.ACTIVE)
            .stream()
            .map(CampaignResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Search campaigns by name
     */
    public List<CampaignResponse> searchCampaigns(String query) {
        logger.info("Searching campaigns with query: {}", query);
        return campaignRepository.findByNameContainingIgnoreCaseAndStatus(query, CampaignStatus.ACTIVE)
            .stream()
            .map(CampaignResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get campaigns by organizer email
     */
    public List<CampaignResponse> getCampaignsByOrganizer(String email) {
        logger.info("Fetching campaigns for organizer: {}", email);
        return campaignRepository.findByOrganizerEmail(email)
            .stream()
            .map(CampaignResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Create a new campaign
     */
    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request) {
        logger.info("Creating new campaign: {}", request.getName());

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setGoalAmount(request.getGoalAmount());
        campaign.setCurrentAmount(BigDecimal.ZERO);
        campaign.setCategory(request.getCategory());
        campaign.setOrganizerName(request.getOrganizerName());
        campaign.setOrganizerEmail(request.getOrganizerEmail());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setImageUrl(request.getImageUrl());

        campaign = campaignRepository.save(campaign);
        logger.info("Campaign created successfully with ID: {}", campaign.getId());

        // Publish campaign created event
        publishEvent(CampaignEvent.created(
            campaign.getId(),
            campaign.getName(),
            campaign.getCategory(),
            campaign.getGoalAmount(),
            campaign.getOrganizerEmail()
        ));

        return CampaignResponse.fromEntity(campaign);
    }

    /**
     * Update an existing campaign
     */
    @Transactional
    public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
        logger.info("Updating campaign with ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setGoalAmount(request.getGoalAmount());
        campaign.setCategory(request.getCategory());
        campaign.setOrganizerName(request.getOrganizerName());
        campaign.setOrganizerEmail(request.getOrganizerEmail());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setImageUrl(request.getImageUrl());

        campaign = campaignRepository.save(campaign);
        logger.info("Campaign updated successfully: {}", campaign.getId());

        // Publish campaign updated event
        publishEvent(CampaignEvent.updated(
            campaign.getId(),
            campaign.getName(),
            campaign.getStatus(),
            campaign.getGoalAmount(),
            campaign.getCurrentAmount()
        ));

        return CampaignResponse.fromEntity(campaign);
    }

    /**
     * Cancel a campaign
     */
    @Transactional
    public void cancelCampaign(Long id) {
        logger.info("Cancelling campaign with ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        campaign.setStatus(CampaignStatus.CANCELLED);
        campaignRepository.save(campaign);

        // Publish campaign cancelled event
        publishEvent(CampaignEvent.cancelled(campaign.getId(), campaign.getName()));

        logger.info("Campaign cancelled successfully: {}", id);
    }

    /**
     * Update campaign donation amount (called by donation service)
     */
    @Transactional
    public CampaignResponse updateDonationAmount(Long id, BigDecimal donationAmount) {
        logger.info("Updating donation amount for campaign ID: {} with amount: {}", id, donationAmount);

        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));

        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new RuntimeException("Cannot donate to inactive campaign");
        }

        BigDecimal previousAmount = campaign.getCurrentAmount();
        campaign.setCurrentAmount(campaign.getCurrentAmount().add(donationAmount));

        // Check if goal is reached
        boolean wasGoalReached = previousAmount.compareTo(campaign.getGoalAmount()) >= 0;
        boolean isGoalReached = campaign.isGoalReached();

        if (!wasGoalReached && isGoalReached) {
            logger.info("Campaign {} has reached its goal!", campaign.getId());
            publishEvent(CampaignEvent.goalReached(
                campaign.getId(),
                campaign.getName(),
                campaign.getGoalAmount(),
                campaign.getCurrentAmount()
            ));
        }

        campaign = campaignRepository.save(campaign);
        logger.info("Campaign donation amount updated successfully: {}", campaign.getId());

        return CampaignResponse.fromEntity(campaign);
    }

    /**
     * Publish campaign event to RabbitMQ
     */
    private void publishEvent(CampaignEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAMPAIGN_EXCHANGE,
                RabbitMQConfig.CAMPAIGN_ROUTING_KEY,
                event
            );
            logger.info("Published campaign event: {} for campaign ID: {}",
                event.getEventType(), event.getCampaignId());
        } catch (Exception e) {
            logger.error("Failed to publish campaign event: {}", e.getMessage(), e);
        }
    }
}
