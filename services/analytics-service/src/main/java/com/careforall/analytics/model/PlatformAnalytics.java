package com.careforall.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "platform_analytics")
public class PlatformAnalytics {

    @Id
    private String id;

    @Builder.Default
    private Integer totalCampaigns = 0;

    @Builder.Default
    private Integer totalDonations = 0;

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    private Integer activeDonors = 0;

    @Builder.Default
    private Integer completedCampaigns = 0;

    @Builder.Default
    private Integer activeCampaigns = 0;

    private LocalDateTime lastUpdated;

    public static final String PLATFORM_ANALYTICS_ID = "platform-analytics-singleton";
}
