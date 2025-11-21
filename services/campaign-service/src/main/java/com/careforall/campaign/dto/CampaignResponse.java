package com.careforall.campaign.dto;

import com.careforall.campaign.entity.Campaign;
import com.careforall.campaign.entity.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Campaign Response DTO
 *
 * Used for returning campaign data to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private String category;
    private String organizerName;
    private String organizerEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private CampaignStatus status;
    private String imageUrl;
    private LocalDateTime createdAt;
    private double percentageAchieved;
    private boolean goalReached;

    public static CampaignResponse fromEntity(Campaign campaign) {
        return new CampaignResponse(
            campaign.getId(),
            campaign.getName(),
            campaign.getDescription(),
            campaign.getGoalAmount(),
            campaign.getCurrentAmount(),
            campaign.getCategory(),
            campaign.getOrganizerName(),
            campaign.getOrganizerEmail(),
            campaign.getStartDate(),
            campaign.getEndDate(),
            campaign.getStatus(),
            campaign.getImageUrl(),
            campaign.getCreatedAt(),
            campaign.getPercentageAchieved(),
            campaign.isGoalReached()
        );
    }
}
