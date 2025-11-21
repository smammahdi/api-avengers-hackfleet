package com.careforall.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Campaign Event
 *
 * Event received from Campaign Service via RabbitMQ for campaign lifecycle events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long campaignId;
    private String campaignName;
    private String organizerName;
    private String organizerEmail;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private Integer totalDonations;
    private String status; // CREATED, COMPLETED, etc.
    private LocalDateTime timestamp;
}
