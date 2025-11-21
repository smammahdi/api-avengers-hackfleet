package com.careforall.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSummary {
    private String campaignId;
    private String campaignName;
    private BigDecimal totalDonated;
    private Integer donationCount;
    private LocalDateTime lastDonationDate;
}
