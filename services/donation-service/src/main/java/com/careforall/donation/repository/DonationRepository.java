package com.careforall.donation.repository;

import com.careforall.donation.entity.Donation;
import com.careforall.donation.entity.DonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Donation Repository
 *
 * Data access layer for Donation entity.
 */
@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    /**
     * Find all donations for a user, ordered by creation date descending
     */
    List<Donation> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all donations for a campaign, ordered by creation date descending
     */
    List<Donation> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);

    /**
     * Find donations by email (for guest donations)
     */
    List<Donation> findByDonorEmailOrderByCreatedAtDesc(String donorEmail);

    /**
     * Find donations by status
     */
    List<Donation> findByStatus(DonationStatus status);

    /**
     * Find all captured donations for a campaign
     */
    List<Donation> findByCampaignIdAndStatus(Long campaignId, DonationStatus status);

    /**
     * Calculate total donations for a campaign (captured only)
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.campaignId = :campaignId AND d.status = 'CAPTURED'")
    BigDecimal calculateTotalDonationsForCampaign(Long campaignId);

    /**
     * Count total donors for a campaign (captured donations only)
     */
    @Query("SELECT COUNT(DISTINCT d.userId) FROM Donation d WHERE d.campaignId = :campaignId AND d.status = 'CAPTURED' AND d.userId IS NOT NULL")
    Long countDonorsForCampaign(Long campaignId);

    /**
     * Find user's donations for a specific campaign
     */
    List<Donation> findByUserIdAndCampaignId(Long userId, Long campaignId);
}
