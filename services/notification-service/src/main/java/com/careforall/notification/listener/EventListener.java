package com.careforall.notification.listener;

import com.careforall.notification.config.RabbitMQConfig;
import com.careforall.notification.event.CampaignEvent;
import com.careforall.notification.event.DonationEvent;
import com.careforall.notification.event.PaymentEvent;
import com.careforall.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Event Listener
 *
 * Listens to RabbitMQ queues for donation, campaign, and payment events.
 */
@Component
public class EventListener {

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * Listen to donation completed events
     */
    @RabbitListener(queues = RabbitMQConfig.DONATION_QUEUE)
    public void handleDonationEvent(DonationEvent event) {
        logger.info("Received donation event: Donation ID {}, Status: {}", event.getDonationId(), event.getStatus());

        try {
            if ("COMPLETED".equals(event.getStatus())) {
                notificationService.sendDonationReceipt(
                    event.getDonorEmail(),
                    event.getDonorName(),
                    event.getDonationId(),
                    event.getCampaignName(),
                    event.getAmount(),
                    event.getTransactionId()
                );
            }
        } catch (Exception e) {
            logger.error("Error processing donation event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to campaign created events
     */
    @RabbitListener(queues = RabbitMQConfig.CAMPAIGN_CREATED_QUEUE)
    public void handleCampaignCreatedEvent(CampaignEvent event) {
        logger.info("Received campaign created event: Campaign ID {}, Name: {}", event.getCampaignId(), event.getCampaignName());

        try {
            notificationService.sendCampaignCreatedNotification(
                event.getCampaignId(),
                event.getCampaignName(),
                event.getOrganizerName(),
                event.getGoalAmount()
            );
        } catch (Exception e) {
            logger.error("Error processing campaign created event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to campaign completed events
     */
    @RabbitListener(queues = RabbitMQConfig.CAMPAIGN_COMPLETED_QUEUE)
    public void handleCampaignCompletedEvent(CampaignEvent event) {
        logger.info("Received campaign completed event: Campaign ID {}, Name: {}", event.getCampaignId(), event.getCampaignName());

        try {
            notificationService.sendCampaignCompletedNotification(
                event.getOrganizerEmail(),
                event.getOrganizerName(),
                event.getCampaignName(),
                event.getGoalAmount(),
                event.getCurrentAmount(),
                event.getTotalDonations()
            );
        } catch (Exception e) {
            logger.error("Error processing campaign completed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen to payment failed events
     */
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailedEvent(PaymentEvent event) {
        logger.info("Received payment failed event: Payment ID {}, Donation ID {}", event.getPaymentId(), event.getDonationId());

        try {
            notificationService.sendPaymentFailedNotification(
                event.getDonorEmail(),
                event.getDonorName(),
                event.getCampaignName(),
                event.getAmount(),
                event.getPaymentId(),
                event.getFailureReason()
            );
        } catch (Exception e) {
            logger.error("Error processing payment failed event: {}", e.getMessage(), e);
        }
    }
}
