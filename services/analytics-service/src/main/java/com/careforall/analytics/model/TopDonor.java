package com.careforall.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopDonor {
    private String userId;
    private String donorName;
    private String donorEmail;
    private BigDecimal totalDonated;
    private Integer donationCount;
}
