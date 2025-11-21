package com.careforall.donation.listener;

import com.careforall.donation.service.DonationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * User Event Listener
 *
 * Listens for UserRegisteredEvent from auth-service to link guest donations
 * when a user registers with an email that was previously used for guest donations.
 *
 * This implements the "Transparent History" feature:
 * - User makes guest donation with email "user@example.com"
 * - Later, user registers with same email
 * - System automatically links all previous guest donations to the new user account
 */
@Component
public class UserEventListener {

    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    @Autowired
    private DonationService donationService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Handle UserRegisteredEvent from auth-service
     * Links all guest donations with matching email to the newly registered user
     */
    @RabbitListener(queues = "user.registered.queue")
    public void handleUserRegistered(String eventJson) {
        try {
            logger.info("Received UserRegisteredEvent: {}", eventJson);

            // Parse event
            UserRegisteredEventPayload event = objectMapper.readValue(eventJson, UserRegisteredEventPayload.class);

            // Link guest donations to the newly registered user
            donationService.linkDonationToUser(event.getEmail(), event.getUserId());

            logger.info("Successfully processed UserRegisteredEvent for user: {} (ID: {})",
                event.getEmail(), event.getUserId());

        } catch (Exception e) {
            logger.error("Failed to process UserRegisteredEvent: {}", e.getMessage(), e);
            // Don't throw - we don't want to trigger redelivery for this async operation
        }
    }

    /**
     * Internal payload class for UserRegisteredEvent
     */
    private static class UserRegisteredEventPayload {
        private Long userId;
        private String email;
        private String name;
        private String registeredAt;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRegisteredAt() {
            return registeredAt;
        }

        public void setRegisteredAt(String registeredAt) {
            this.registeredAt = registeredAt;
        }
    }
}
