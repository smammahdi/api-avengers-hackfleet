package com.careforall.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "campaign_analytics")
public class CampaignAnalytics {

    @Id
    private String id;

    private String campaignId;
    private String name;
    private String description;

    @Builder.Default
    private BigDecimal totalDonations = BigDecimal.ZERO;

    @Builder.Default
    private Integer donorCount = 0;

    @Builder.Default
    private BigDecimal averageDonation = BigDecimal.ZERO;

    private BigDecimal goalAmount;

    @Builder.Default
    private BigDecimal goalProgress = BigDecimal.ZERO;

    private LocalDateTime lastDonationDate;
    private LocalDateTime createdDate;
    private LocalDateTime completedDate;

    @Builder.Default
    private List<TopDonor> topDonors = new ArrayList<>();

    private String status; // ACTIVE, COMPLETED, CANCELLED

    public void calculateAverageDonation() {
        if (donorCount > 0) {
            this.averageDonation = totalDonations.divide(
                BigDecimal.valueOf(donorCount), 2, BigDecimal.ROUND_HALF_UP
            );
        } else {
            this.averageDonation = BigDecimal.ZERO;
        }
    }

    public void calculateGoalProgress() {
        if (goalAmount != null && goalAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.goalProgress = totalDonations
                .divide(goalAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        } else {
            this.goalProgress = BigDecimal.ZERO;
        }
    }
}
