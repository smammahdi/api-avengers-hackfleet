package com.careforall.campaign.repository;

import com.careforall.campaign.entity.Campaign;
import com.careforall.campaign.entity.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Campaign Repository
 *
 * Data access layer for Campaign entity.
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    /**
     * Find all active campaigns
     */
    List<Campaign> findByStatus(CampaignStatus status);

    /**
     * Find campaigns by category
     */
    List<Campaign> findByCategoryAndStatus(String category, CampaignStatus status);

    /**
     * Find campaigns by organizer email
     */
    List<Campaign> findByOrganizerEmail(String organizerEmail);

    /**
     * Find campaigns by name containing (case-insensitive search)
     */
    List<Campaign> findByNameContainingIgnoreCaseAndStatus(String name, CampaignStatus status);

    /**
     * Find all active campaigns (convenience method)
     */
    default List<Campaign> findActiveCampaigns() {
        return findByStatus(CampaignStatus.ACTIVE);
    }
}
