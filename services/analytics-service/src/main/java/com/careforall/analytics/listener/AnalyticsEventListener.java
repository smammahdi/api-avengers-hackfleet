package com.careforall.analytics.listener;

import com.careforall.analytics.dto.CampaignCompletedEvent;
import com.careforall.analytics.dto.CampaignCreatedEvent;
import com.careforall.analytics.dto.DonationCompletedEvent;
import com.careforall.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens to donation and campaign events from RabbitMQ to update analytics in real-time.
 * This implements the CQRS pattern where write operations happen in other services,
 * and this service maintains denormalized read models for analytics queries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {

    private final AnalyticsService analyticsService;

    /**
     * Processes DONATION_COMPLETED events to update campaign and donor analytics.
     * This ensures analytics are updated atomically when donations complete.
     */
    @RabbitListener(queues = "${rabbitmq.queue.donation.completed}")
    public void handleDonationCompleted(DonationCompletedEvent event) {
        try {
            log.info("Received DonationCompleted event: campaignId={}, amount={}, donorEmail={}", 
                event.getCampaignId(), event.getAmount(), event.getDonorEmail());
            
            analyticsService.processDonationCompleted(event);
            
            log.info("Successfully processed DonationCompleted event for campaignId={}", event.getCampaignId());
        } catch (Exception e) {
            log.error("Error processing DonationCompleted event: campaignId={}, error={}", 
                event.getCampaignId(), e.getMessage(), e);
            // In production, implement dead-letter queue and retry logic
        }
    }

    /**
     * Processes CAMPAIGN_CREATED events to initialize analytics records.
     */
    @RabbitListener(queues = "${rabbitmq.queue.campaign.created}")
    public void handleCampaignCreated(CampaignCreatedEvent event) {
        try {
            log.info("Received CampaignCreated event: campaignId={}, name={}", 
                event.getCampaignId(), event.getName());
            
            analyticsService.processCampaignCreated(event);
            
            log.info("Successfully processed CampaignCreated event for campaignId={}", event.getCampaignId());
        } catch (Exception e) {
            log.error("Error processing CampaignCreated event: campaignId={}, error={}", 
                event.getCampaignId(), e.getMessage(), e);
        }
    }

    /**
     * Processes CAMPAIGN_COMPLETED events to finalize campaign analytics.
     */
    @RabbitListener(queues = "${rabbitmq.queue.campaign.completed}")
    public void handleCampaignCompleted(CampaignCompletedEvent event) {
        try {
            log.info("Received CampaignCompleted event: campaignId={}", event.getCampaignId());
            
            analyticsService.processCampaignCompleted(event);
            
            log.info("Successfully processed CampaignCompleted event for campaignId={}", event.getCampaignId());
        } catch (Exception e) {
            log.error("Error processing CampaignCompleted event: campaignId={}, error={}", 
                event.getCampaignId(), e.getMessage(), e);
        }
    }
}
