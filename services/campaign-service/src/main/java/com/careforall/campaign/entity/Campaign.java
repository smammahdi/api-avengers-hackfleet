package com.careforall.campaign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Campaign Entity
 *
 * Represents a fundraising campaign in the Care for All platform.
 */
@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "goal_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal goalAmount;

    @Column(name = "current_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String category;

    @Column(name = "organizer_name", nullable = false)
    private String organizerName;

    @Column(name = "organizer_email", nullable = false)
    private String organizerEmail;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status = CampaignStatus.ACTIVE;

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calculate the percentage of goal achieved
     */
    public double getPercentageAchieved() {
        if (goalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return currentAmount.divide(goalAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Check if campaign goal is reached
     */
    public boolean isGoalReached() {
        return currentAmount.compareTo(goalAmount) >= 0;
    }

    /**
     * Check if campaign has ended
     */
    public boolean hasEnded() {
        return LocalDate.now().isAfter(endDate);
    }
}
