package com.careforall.analytics.service;

import com.careforall.analytics.dto.CampaignCompletedEvent;
import com.careforall.analytics.dto.CampaignCreatedEvent;
import com.careforall.analytics.dto.DonationCompletedEvent;
import com.careforall.analytics.model.*;
import com.careforall.analytics.repository.CampaignAnalyticsRepository;
import com.careforall.analytics.repository.DonorAnalyticsRepository;
import com.careforall.analytics.repository.PlatformAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final CampaignAnalyticsRepository campaignAnalyticsRepository;
    private final DonorAnalyticsRepository donorAnalyticsRepository;
    private final PlatformAnalyticsRepository platformAnalyticsRepository;

    @Transactional
    public void handleDonationCompleted(DonationCompletedEvent event) {
        log.info("Processing donation completed event: {}", event.getDonationId());

        // Update Campaign Analytics
        updateCampaignAnalytics(event);

        // Update Donor Analytics
        updateDonorAnalytics(event);

        // Update Platform Analytics
        updatePlatformAnalytics(event);

        log.info("Successfully processed donation completed event: {}", event.getDonationId());
    }

    /**
     * Alias for handleDonationCompleted (called by event listener)
     */
    @Transactional
    public void processDonationCompleted(DonationCompletedEvent event) {
        handleDonationCompleted(event);
    }

    @Transactional
    public void handleCampaignCreated(CampaignCreatedEvent event) {
        log.info("Processing campaign created event: {}", event.getCampaignId());

        CampaignAnalytics analytics = CampaignAnalytics.builder()
                .campaignId(event.getCampaignId())
                .name(event.getName())
                .description(event.getDescription())
                .goalAmount(event.getGoalAmount())
                .totalDonations(BigDecimal.ZERO)
                .donorCount(0)
                .averageDonation(BigDecimal.ZERO)
                .goalProgress(BigDecimal.ZERO)
                .topDonors(new ArrayList<>())
                .createdDate(LocalDateTime.now())
                .status(event.getStatus())
                .build();

        campaignAnalyticsRepository.save(analytics);

        // Update platform analytics
        PlatformAnalytics platformAnalytics = getPlatformAnalytics();
        platformAnalytics.setTotalCampaigns(platformAnalytics.getTotalCampaigns() + 1);
        platformAnalytics.setActiveCampaigns(platformAnalytics.getActiveCampaigns() + 1);
        platformAnalytics.setLastUpdated(LocalDateTime.now());
        platformAnalyticsRepository.save(platformAnalytics);

        log.info("Successfully created campaign analytics: {}", event.getCampaignId());
    }

    /**
     * Alias for handleCampaignCreated (called by event listener)
     */
    @Transactional
    public void processCampaignCreated(CampaignCreatedEvent event) {
        handleCampaignCreated(event);
    }

    @Transactional
    public void handleCampaignCompleted(CampaignCompletedEvent event) {
        log.info("Processing campaign completed event: {}", event.getCampaignId());

        Optional<CampaignAnalytics> analyticsOpt = campaignAnalyticsRepository.findByCampaignId(event.getCampaignId());
        if (analyticsOpt.isPresent()) {
            CampaignAnalytics analytics = analyticsOpt.get();
            analytics.setStatus(event.getStatus());
            analytics.setCompletedDate(event.getCompletedDate());
            campaignAnalyticsRepository.save(analytics);

            // Update platform analytics
            PlatformAnalytics platformAnalytics = getPlatformAnalytics();
            platformAnalytics.setActiveCampaigns(Math.max(0, platformAnalytics.getActiveCampaigns() - 1));
            if ("COMPLETED".equals(event.getStatus())) {
                platformAnalytics.setCompletedCampaigns(platformAnalytics.getCompletedCampaigns() + 1);
            }
            platformAnalytics.setLastUpdated(LocalDateTime.now());
            platformAnalyticsRepository.save(platformAnalytics);

            log.info("Successfully updated campaign status: {}", event.getCampaignId());
        } else {
            log.warn("Campaign analytics not found: {}", event.getCampaignId());
        }
    }

    /**
     * Alias for handleCampaignCompleted (called by event listener)
     */
    @Transactional
    public void processCampaignCompleted(CampaignCompletedEvent event) {
        handleCampaignCompleted(event);
    }

    private void updateCampaignAnalytics(DonationCompletedEvent event) {
        Optional<CampaignAnalytics> analyticsOpt = campaignAnalyticsRepository.findByCampaignId(event.getCampaignId());

        CampaignAnalytics analytics;
        if (analyticsOpt.isPresent()) {
            analytics = analyticsOpt.get();
        } else {
            // Create if not exists
            analytics = CampaignAnalytics.builder()
                    .campaignId(event.getCampaignId())
                    .name(event.getCampaignName())
                    .totalDonations(BigDecimal.ZERO)
                    .donorCount(0)
                    .averageDonation(BigDecimal.ZERO)
                    .goalProgress(BigDecimal.ZERO)
                    .topDonors(new ArrayList<>())
                    .createdDate(LocalDateTime.now())
                    .status("ACTIVE")
                    .build();
        }

        // Update totals
        analytics.setTotalDonations(analytics.getTotalDonations().add(event.getAmount()));
        analytics.setDonorCount(analytics.getDonorCount() + 1);
        analytics.setLastDonationDate(event.getDonationDate());

        // Calculate averages and progress
        analytics.calculateAverageDonation();
        analytics.calculateGoalProgress();

        // Update top donors
        updateTopDonors(analytics, event);

        campaignAnalyticsRepository.save(analytics);
    }

    private void updateTopDonors(CampaignAnalytics analytics, DonationCompletedEvent event) {
        List<TopDonor> topDonors = analytics.getTopDonors();

        // Find existing donor or create new
        Optional<TopDonor> existingDonor = topDonors.stream()
                .filter(d -> d.getUserId().equals(event.getUserId()))
                .findFirst();

        if (existingDonor.isPresent()) {
            TopDonor donor = existingDonor.get();
            donor.setTotalDonated(donor.getTotalDonated().add(event.getAmount()));
            donor.setDonationCount(donor.getDonationCount() + 1);
        } else {
            TopDonor newDonor = TopDonor.builder()
                    .userId(event.getUserId())
                    .donorName(event.getDonorName())
                    .donorEmail(event.getDonorEmail())
                    .totalDonated(event.getAmount())
                    .donationCount(1)
                    .build();
            topDonors.add(newDonor);
        }

        // Sort and keep top 10
        topDonors.sort(Comparator.comparing(TopDonor::getTotalDonated).reversed());
        if (topDonors.size() > 10) {
            analytics.setTopDonors(topDonors.subList(0, 10));
        }
    }

    private void updateDonorAnalytics(DonationCompletedEvent event) {
        Optional<DonorAnalytics> analyticsOpt = donorAnalyticsRepository.findByUserId(event.getUserId());

        DonorAnalytics analytics;
        boolean isNewDonor = false;

        if (analyticsOpt.isPresent()) {
            analytics = analyticsOpt.get();
        } else {
            isNewDonor = true;
            analytics = DonorAnalytics.builder()
                    .userId(event.getUserId())
                    .donorEmail(event.getDonorEmail())
                    .donorName(event.getDonorName())
                    .totalDonated(BigDecimal.ZERO)
                    .donationCount(0)
                    .campaigns(new ArrayList<>())
                    .firstDonationDate(event.getDonationDate())
                    .build();
        }

        // Update totals
        analytics.setTotalDonated(analytics.getTotalDonated().add(event.getAmount()));
        analytics.setDonationCount(analytics.getDonationCount() + 1);
        analytics.setLastDonationDate(event.getDonationDate());

        // Update campaign summary
        updateCampaignSummary(analytics, event);

        donorAnalyticsRepository.save(analytics);

        // Update platform active donors count if new donor
        if (isNewDonor) {
            PlatformAnalytics platformAnalytics = getPlatformAnalytics();
            platformAnalytics.setActiveDonors(platformAnalytics.getActiveDonors() + 1);
            platformAnalytics.setLastUpdated(LocalDateTime.now());
            platformAnalyticsRepository.save(platformAnalytics);
        }
    }

    private void updateCampaignSummary(DonorAnalytics analytics, DonationCompletedEvent event) {
        List<CampaignSummary> campaigns = analytics.getCampaigns();

        Optional<CampaignSummary> existingCampaign = campaigns.stream()
                .filter(c -> c.getCampaignId().equals(event.getCampaignId()))
                .findFirst();

        if (existingCampaign.isPresent()) {
            CampaignSummary campaign = existingCampaign.get();
            campaign.setTotalDonated(campaign.getTotalDonated().add(event.getAmount()));
            campaign.setDonationCount(campaign.getDonationCount() + 1);
            campaign.setLastDonationDate(event.getDonationDate());
        } else {
            CampaignSummary newCampaign = CampaignSummary.builder()
                    .campaignId(event.getCampaignId())
                    .campaignName(event.getCampaignName())
                    .totalDonated(event.getAmount())
                    .donationCount(1)
                    .lastDonationDate(event.getDonationDate())
                    .build();
            campaigns.add(newCampaign);
        }
    }

    private void updatePlatformAnalytics(DonationCompletedEvent event) {
        PlatformAnalytics analytics = getPlatformAnalytics();

        analytics.setTotalDonations(analytics.getTotalDonations() + 1);
        analytics.setTotalAmount(analytics.getTotalAmount().add(event.getAmount()));
        analytics.setLastUpdated(LocalDateTime.now());

        platformAnalyticsRepository.save(analytics);
    }

    public CampaignAnalytics getCampaignAnalytics(String campaignId) {
        return campaignAnalyticsRepository.findByCampaignId(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign analytics not found: " + campaignId));
    }

    public List<CampaignAnalytics> getTopCampaigns(int limit) {
        return campaignAnalyticsRepository.findByOrderByTotalDonationsDesc(PageRequest.of(0, limit));
    }

    public DonorAnalytics getDonorAnalytics(String userId) {
        return donorAnalyticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Donor analytics not found: " + userId));
    }

    public Optional<DonorAnalytics> getDonorAnalyticsByEmail(String email) {
        log.info("Fetching donor analytics by email: {}", email);
        return donorAnalyticsRepository.findByDonorEmail(email);
    }

    public PlatformAnalytics getPlatformAnalytics() {
        return platformAnalyticsRepository.findById(PlatformAnalytics.PLATFORM_ANALYTICS_ID)
                .orElseGet(() -> {
                    PlatformAnalytics analytics = PlatformAnalytics.builder()
                            .id(PlatformAnalytics.PLATFORM_ANALYTICS_ID)
                            .totalCampaigns(0)
                            .totalDonations(0)
                            .totalAmount(BigDecimal.ZERO)
                            .activeDonors(0)
                            .completedCampaigns(0)
                            .activeCampaigns(0)
                            .lastUpdated(LocalDateTime.now())
                            .build();
                    return platformAnalyticsRepository.save(analytics);
                });
    }
}
