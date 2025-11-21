package com.careforall.donation.outbox;

import com.careforall.donation.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Outbox Publisher
 *
 * Scheduled task that polls the outbox table and publishes pending events to RabbitMQ.
 * Runs every 5 seconds to ensure eventual consistency.
 */
@Service
public class OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int MAX_RETRIES = 3;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Scheduled task that runs every 5 seconds to publish pending events
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        logger.info("Found {} pending events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
            } catch (Exception e) {
                logger.error("Failed to publish event {}: {}", event.getId(), e.getMessage());
                handlePublishFailure(event, e);
            }
        }
    }

    /**
     * Publish a single event to RabbitMQ
     */
    private void publishEvent(OutboxEvent event) {
        logger.debug("Publishing event: {} - {}", event.getEventType(), event.getAggregateId());

        // Update status to PROCESSING to avoid duplicate processing
        event.setStatus(OutboxEvent.EventStatus.PROCESSING);
        outboxEventRepository.save(event);

        try {
            // Parse the payload and send to RabbitMQ
            Object payloadObject = objectMapper.readValue(event.getPayload(), Object.class);

            // Determine routing key based on event type
            String routingKey = determineRoutingKey(event.getEventType());

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.DONATION_EXCHANGE,
                routingKey,
                payloadObject
            );

            // Mark as published
            event.markAsPublished();
            outboxEventRepository.save(event);

            logger.info("Successfully published event {} - {} to {}",
                event.getId(), event.getEventType(), routingKey);

        } catch (Exception e) {
            // Revert status back to PENDING on error
            event.setStatus(OutboxEvent.EventStatus.PENDING);
            outboxEventRepository.save(event);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    /**
     * Handle event publishing failure
     */
    private void handlePublishFailure(OutboxEvent event, Exception e) {
        event.incrementRetryCount();

        if (event.getRetryCount() >= MAX_RETRIES) {
            event.markAsFailed(e.getMessage());
            logger.error("Event {} failed after {} retries. Moving to FAILED status.",
                event.getId(), MAX_RETRIES);
        } else {
            event.setStatus(OutboxEvent.EventStatus.PENDING);
            logger.warn("Event {} failed, retry count: {}", event.getId(), event.getRetryCount());
        }

        outboxEventRepository.save(event);
    }

    /**
     * Determine routing key based on event type
     */
    private String determineRoutingKey(String eventType) {
        return switch (eventType) {
            case "DONATION_CREATED" -> RabbitMQConfig.DONATION_CREATED_ROUTING_KEY;
            case "DONATION_COMPLETED" -> RabbitMQConfig.DONATION_COMPLETED_ROUTING_KEY;
            case "DONATION_FAILED" -> RabbitMQConfig.DONATION_FAILED_ROUTING_KEY;
            case "DONATION_REFUNDED" -> RabbitMQConfig.DONATION_REFUNDED_ROUTING_KEY;
            default -> "donation.unknown";
        };
    }

    /**
     * Get pending events count for monitoring
     */
    public long getPendingEventsCount() {
        return outboxEventRepository.countByStatus(OutboxEvent.EventStatus.PENDING);
    }

    /**
     * Get failed events count for monitoring
     */
    public long getFailedEventsCount() {
        return outboxEventRepository.countByStatus(OutboxEvent.EventStatus.FAILED);
    }
}
