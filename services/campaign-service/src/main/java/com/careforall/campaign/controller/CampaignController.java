package com.careforall.campaign.controller;

import com.careforall.campaign.dto.CampaignRequest;
import com.careforall.campaign.dto.CampaignResponse;
import com.careforall.campaign.service.CampaignService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Campaign Controller
 *
 * REST API endpoints for campaign management.
 */
@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private static final Logger logger = LoggerFactory.getLogger(CampaignController.class);

    @Autowired
    private CampaignService campaignService;

    /**
     * Get all active campaigns
     */
    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getAllCampaigns() {
        List<CampaignResponse> campaigns = campaignService.getAllCampaigns();
        return ResponseEntity.ok(campaigns);
    }

    /**
     * Get campaign by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCampaignById(@PathVariable Long id) {
        try {
            CampaignResponse campaign = campaignService.getCampaignById(id);
            return ResponseEntity.ok(campaign);
        } catch (Exception e) {
            logger.error("Failed to get campaign: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Get campaigns by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CampaignResponse>> getCampaignsByCategory(@PathVariable String category) {
        List<CampaignResponse> campaigns = campaignService.getCampaignsByCategory(category);
        return ResponseEntity.ok(campaigns);
    }

    /**
     * Search campaigns
     */
    @GetMapping("/search")
    public ResponseEntity<List<CampaignResponse>> searchCampaigns(@RequestParam String query) {
        List<CampaignResponse> campaigns = campaignService.searchCampaigns(query);
        return ResponseEntity.ok(campaigns);
    }

    /**
     * Get campaigns by organizer email
     */
    @GetMapping("/organizer/{email}")
    public ResponseEntity<List<CampaignResponse>> getCampaignsByOrganizer(@PathVariable String email) {
        List<CampaignResponse> campaigns = campaignService.getCampaignsByOrganizer(email);
        return ResponseEntity.ok(campaigns);
    }

    /**
     * Create a new campaign (authenticated users only)
     */
    @PostMapping
    public ResponseEntity<?> createCampaign(
        @Valid @RequestBody CampaignRequest request,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        try {
            // In a real system, the organizer email would be set from authenticated user
            if (userEmail != null && !userEmail.equals(request.getOrganizerEmail())) {
                logger.warn("User email mismatch: {} vs {}", userEmail, request.getOrganizerEmail());
            }

            CampaignResponse campaign = campaignService.createCampaign(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(campaign);
        } catch (Exception e) {
            logger.error("Failed to create campaign: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Update a campaign (organizer only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampaign(
        @PathVariable Long id,
        @Valid @RequestBody CampaignRequest request,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        try {
            // In a real system, verify that the user is the organizer
            CampaignResponse campaign = campaignService.updateCampaign(id, request);
            return ResponseEntity.ok(campaign);
        } catch (Exception e) {
            logger.error("Failed to update campaign: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Cancel a campaign (organizer or admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelCampaign(
        @PathVariable Long id,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        try {
            // In a real system, verify that the user is the organizer or admin
            campaignService.cancelCampaign(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Campaign cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to cancel campaign: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Update campaign donation amount (internal service call)
     * This endpoint is called by the donation service when a donation is made
     */
    @PutMapping("/{id}/donation")
    public ResponseEntity<?> updateDonationAmount(
        @PathVariable Long id,
        @RequestParam BigDecimal amount
    ) {
        try {
            CampaignResponse campaign = campaignService.updateDonationAmount(id, amount);
            return ResponseEntity.ok(campaign);
        } catch (Exception e) {
            logger.error("Failed to update donation amount: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "hf-campaign-service");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create error response
     */
    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
