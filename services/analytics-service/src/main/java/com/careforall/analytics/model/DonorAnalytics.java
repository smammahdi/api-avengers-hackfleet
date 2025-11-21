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
@Document(collection = "donor_analytics")
public class DonorAnalytics {

    @Id
    private String id;

    private String userId;
    private String donorEmail;
    private String donorName;

    @Builder.Default
    private BigDecimal totalDonated = BigDecimal.ZERO;

    @Builder.Default
    private Integer donationCount = 0;

    @Builder.Default
    private List<CampaignSummary> campaigns = new ArrayList<>();

    private LocalDateTime lastDonationDate;
    private LocalDateTime firstDonationDate;
}
