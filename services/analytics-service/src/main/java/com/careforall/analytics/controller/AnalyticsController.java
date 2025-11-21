package com.careforall.analytics.controller;

import com.careforall.analytics.model.CampaignAnalytics;
import com.careforall.analytics.model.DonorAnalytics;
import com.careforall.analytics.model.PlatformAnalytics;
import com.careforall.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for querying analytics data.
 * Provides read-only endpoints for campaign, donor, and platform-wide analytics.
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get campaign analytics", description = "Retrieve analytics for a specific campaign")
    @GetMapping("/campaigns/{campaignId}")
    public ResponseEntity<CampaignAnalytics> getCampaignAnalytics(@PathVariable String campaignId) {
        log.info("Fetching analytics for campaignId={}", campaignId);
        try {
            CampaignAnalytics analytics = analyticsService.getCampaignAnalytics(campaignId);
            return ResponseEntity.ok(analytics);
        } catch (RuntimeException e) {
            log.warn("Campaign analytics not found for campaignId={}", campaignId);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get top campaigns", description = "Get top campaigns by donation amount")
    @GetMapping("/campaigns/top")
    public ResponseEntity<List<CampaignAnalytics>> getTopCampaigns(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching top {} campaigns", limit);
        return ResponseEntity.ok(analyticsService.getTopCampaigns(limit));
    }

    @Operation(summary = "Get donor analytics", description = "Retrieve analytics for a specific donor")
    @GetMapping("/donors/{userId}")
    public ResponseEntity<DonorAnalytics> getDonorAnalytics(@PathVariable String userId) {
        log.info("Fetching analytics for userId={}", userId);
        try {
            DonorAnalytics analytics = analyticsService.getDonorAnalytics(userId);
            return ResponseEntity.ok(analytics);
        } catch (RuntimeException e) {
            log.warn("Donor analytics not found for userId={}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get donor analytics by email", description = "Retrieve analytics for a donor by email")
    @GetMapping("/donors")
    public ResponseEntity<DonorAnalytics> getDonorAnalyticsByEmail(@RequestParam String email) {
        log.info("Fetching analytics for email={}", email);
        return analyticsService.getDonorAnalyticsByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get platform-wide analytics", description = "Retrieve aggregated platform statistics")
    @GetMapping("/platform")
    public ResponseEntity<PlatformAnalytics> getPlatformAnalytics() {
        log.info("Fetching platform-wide analytics");
        return ResponseEntity.ok(analyticsService.getPlatformAnalytics());
    }

    @Operation(summary = "Health check", description = "Check analytics service health")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics service is healthy");
    }
}
