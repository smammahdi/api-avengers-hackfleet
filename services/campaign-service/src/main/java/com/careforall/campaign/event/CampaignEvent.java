package com.careforall.campaign.event;

import com.careforall.campaign.entity.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Campaign Event
 *
 * Represents events related to campaigns that are published to RabbitMQ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum EventType {
        CAMPAIGN_CREATED,
        CAMPAIGN_UPDATED,
        CAMPAIGN_CANCELLED,
        CAMPAIGN_COMPLETED,
        GOAL_REACHED
    }

    private Long campaignId;
    private String campaignName;
    private EventType eventType;
    private CampaignStatus status;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private String category;
    private String organizerEmail;
    private LocalDateTime timestamp;

    public static CampaignEvent created(Long campaignId, String campaignName, String category,
                                       BigDecimal goalAmount, String organizerEmail) {
        CampaignEvent event = new CampaignEvent();
        event.setCampaignId(campaignId);
        event.setCampaignName(campaignName);
        event.setEventType(EventType.CAMPAIGN_CREATED);
        event.setStatus(CampaignStatus.ACTIVE);
        event.setCategory(category);
        event.setGoalAmount(goalAmount);
        event.setCurrentAmount(BigDecimal.ZERO);
        event.setOrganizerEmail(organizerEmail);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    public static CampaignEvent updated(Long campaignId, String campaignName, CampaignStatus status,
                                       BigDecimal goalAmount, BigDecimal currentAmount) {
        CampaignEvent event = new CampaignEvent();
        event.setCampaignId(campaignId);
        event.setCampaignName(campaignName);
        event.setEventType(EventType.CAMPAIGN_UPDATED);
        event.setStatus(status);
        event.setGoalAmount(goalAmount);
        event.setCurrentAmount(currentAmount);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    public static CampaignEvent cancelled(Long campaignId, String campaignName) {
        CampaignEvent event = new CampaignEvent();
        event.setCampaignId(campaignId);
        event.setCampaignName(campaignName);
        event.setEventType(EventType.CAMPAIGN_CANCELLED);
        event.setStatus(CampaignStatus.CANCELLED);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    public static CampaignEvent goalReached(Long campaignId, String campaignName,
                                           BigDecimal goalAmount, BigDecimal currentAmount) {
        CampaignEvent event = new CampaignEvent();
        event.setCampaignId(campaignId);
        event.setCampaignName(campaignName);
        event.setEventType(EventType.GOAL_REACHED);
        event.setStatus(CampaignStatus.ACTIVE);
        event.setGoalAmount(goalAmount);
        event.setCurrentAmount(currentAmount);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
