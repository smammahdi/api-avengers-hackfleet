package com.careforall.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCompletedEvent {
    private String campaignId;
    private String name;
    private String status; // COMPLETED, CANCELLED
    private LocalDateTime completedDate;
}
