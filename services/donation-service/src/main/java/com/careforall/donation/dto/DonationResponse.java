package com.careforall.donation.dto;

import com.careforall.donation.entity.Donation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Donation Response DTO
 *
 * Response payload for donation operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {

    private UUID id;
    private Long campaignId;
    private Long userId;
    private BigDecimal amount;
    private String donorName;
    private String donorEmail;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private String message;
    private Boolean isAnonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    /**
     * Convert Donation entity to response DTO
     */
    public static DonationResponse fromEntity(Donation donation) {
        return new DonationResponse(
            donation.getId(),
            donation.getCampaignId(),
            donation.getUserId(),
            donation.getAmount(),
            donation.getDonorName(),
            donation.getDonorEmail(),
            donation.getPaymentMethod(),
            donation.getStatus().name(),
            donation.getTransactionId(),
            donation.getMessage(),
            donation.getIsAnonymous(),
            donation.getCreatedAt(),
            donation.getUpdatedAt(),
            donation.getCompletedAt()
        );
    }
}
