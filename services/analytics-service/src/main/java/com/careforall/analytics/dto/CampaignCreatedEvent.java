package com.careforall.analytics.dto;

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
public class CampaignCreatedEvent {
    private String campaignId;
    private String name;
    private String description;
    private BigDecimal goalAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String organizerId;
    private String status;
}
