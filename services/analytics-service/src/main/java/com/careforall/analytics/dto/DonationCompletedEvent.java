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
public class DonationCompletedEvent {
    private String donationId;
    private String campaignId;
    private String campaignName;
    private String userId;
    private String donorEmail;
    private String donorName;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime donationDate;
    private String status;
}
