package com.careforall.donation.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Outbox Event Repository
 *
 * Data access layer for OutboxEvent entity.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Find all pending events ordered by creation time
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEvent.EventStatus status);

    /**
     * Find pending events that need to be published
     * Includes failed events with retry count less than max retries
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' OR (e.status = 'FAILED' AND e.retryCount < 3) ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEvents();

    /**
     * Count events by status
     */
    long countByStatus(OutboxEvent.EventStatus status);
}
