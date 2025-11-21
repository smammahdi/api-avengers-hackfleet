package com.careforall.donation.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Outbox Event Entity
 *
 * Implements the Transactional Outbox Pattern for reliable event publishing.
 * Events are stored in the database in the same transaction as the business entity,
 * then asynchronously published to the message broker.
 */
@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID of the aggregate (e.g., donation ID as UUID string)
     */
    @Column(nullable = false, length = 255)
    private String aggregateId;

    /**
     * Type of aggregate (e.g., "DONATION")
     */
    @Column(nullable = false, length = 50)
    private String aggregateType;

    /**
     * Type of event (e.g., "DONATION_CREATED", "DONATION_COMPLETED")
     */
    @Column(nullable = false, length = 50)
    private String eventType;

    /**
     * Event payload as JSON string
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    /**
     * Processing status of the event
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    /**
     * Timestamp when the event was created
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the event was processed
     */
    private LocalDateTime processedAt;

    /**
     * Number of retry attempts
     */
    @Column(nullable = false)
    private Integer retryCount = 0;

    /**
     * Error message if processing failed
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Event status enum
     */
    public enum EventStatus {
        PENDING,
        PROCESSING,
        PUBLISHED,
        FAILED
    }

    /**
     * Factory method to create a new outbox event
     */
    public static OutboxEvent create(String aggregateId, String aggregateType, String eventType, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateId(aggregateId);
        event.setAggregateType(aggregateType);
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setStatus(EventStatus.PENDING);
        event.setRetryCount(0);
        return event;
    }

    /**
     * Mark event as published
     */
    public void markAsPublished() {
        this.status = EventStatus.PUBLISHED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark event as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = EventStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
}
