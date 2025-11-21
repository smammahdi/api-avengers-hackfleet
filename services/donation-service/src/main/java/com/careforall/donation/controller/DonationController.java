package com.careforall.donation.controller;

import com.careforall.donation.dto.CreateDonationRequest;
import com.careforall.donation.dto.DonationResponse;
import com.careforall.donation.service.DonationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Donation Controller
 *
 * REST API endpoints for donation management.
 */
@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private static final Logger logger = LoggerFactory.getLogger(DonationController.class);

    @Autowired
    private DonationService donationService;

    /**
     * Create a new donation
     * Requires authentication - user ID comes from JWT header
     */
    @PostMapping
    public ResponseEntity<?> createDonation(
        @Valid @RequestBody CreateDonationRequest request,
        @RequestHeader("X-User-Id") String userId
    ) {
        try {
            DonationResponse donation = donationService.createDonation(request, Long.parseLong(userId));
            return ResponseEntity.status(HttpStatus.CREATED).body(donation);
        } catch (Exception e) {
            logger.error("Failed to create donation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Get donation by ID
     */
    @GetMapping("/{donationId}")
    public ResponseEntity<?> getDonationById(
        @PathVariable UUID donationId,
        @RequestHeader("X-User-Id") String userId
    ) {
        try {
            DonationResponse donation = donationService.getDonationById(donationId);

            // Verify donation belongs to user (or allow if admin)
            if (donation.getUserId() != null && !donation.getUserId().equals(Long.parseLong(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse("Unauthorized to view this donation"));
            }

            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            logger.error("Failed to get donation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Get all donations for authenticated user
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserDonations(@RequestHeader("X-User-Id") String userId) {
        try {
            List<DonationResponse> donations = donationService.getUserDonations(Long.parseLong(userId));
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            logger.error("Failed to get user donations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Get all donations for a specific campaign
     */
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<?> getCampaignDonations(@PathVariable Long campaignId) {
        try {
            List<DonationResponse> donations = donationService.getCampaignDonations(campaignId);
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            logger.error("Failed to get campaign donations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Refund a donation
     */
    // Refund functionality not implemented yet
    // TODO: Implement refundDonation in DonationService
    /*
    @PostMapping("/{donationId}/refund")
    public ResponseEntity<?> refundDonation(
        @PathVariable Long donationId,
        @RequestHeader("X-User-Id") String userId
    ) {
        try {
            DonationResponse donation = donationService.refundDonation(donationId, Long.parseLong(userId));
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            logger.error("Failed to refund donation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }
    */

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "hf-donation-service");
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
