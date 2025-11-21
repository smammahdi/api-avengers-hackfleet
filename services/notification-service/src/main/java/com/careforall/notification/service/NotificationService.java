package com.careforall.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Notification Service
 *
 * Mock implementation for sending email notifications for CareForAll platform.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String ADMIN_EMAIL = "admin@careforall.org";

    /**
     * Send donation receipt email to donor
     */
    public void sendDonationReceipt(String email, String donorName, Long donationId,
                                    String campaignName, BigDecimal amount, String transactionId) {
        logger.info("==============================================");
        logger.info("SENDING DONATION RECEIPT EMAIL");
        logger.info("==============================================");
        logger.info("To: {}", email);
        logger.info("Subject: Donation Receipt - Thank You for Your Support!");
        logger.info("Body:");
        logger.info("  Dear {},", donorName);
        logger.info("  ");
        logger.info("  Thank you for your generous donation to CareForAll!");
        logger.info("  ");
        logger.info("  DONATION RECEIPT");
        logger.info("  ----------------");
        logger.info("  Donor Name: {}", donorName);
        logger.info("  Donation ID: {}", donationId);
        logger.info("  Campaign: {}", campaignName);
        logger.info("  Amount: ${}", amount);
        logger.info("  Transaction ID: {}", transactionId);
        logger.info("  Date: {}", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logger.info("  ");
        logger.info("  TAX RECEIPT INFORMATION");
        logger.info("  -----------------------");
        logger.info("  This donation is tax-deductible to the extent allowed by law.");
        logger.info("  CareForAll is a registered 501(c)(3) nonprofit organization.");
        logger.info("  Tax ID: 12-3456789");
        logger.info("  ");
        logger.info("  Please retain this receipt for your tax records.");
        logger.info("  ");
        logger.info("  Your contribution helps make a difference in the lives of those in need.");
        logger.info("  ");
        logger.info("  With gratitude,");
        logger.info("  The CareForAll Team");
        logger.info("==============================================");
    }

    /**
     * Send campaign created notification to platform admins
     */
    public void sendCampaignCreatedNotification(Long campaignId, String campaignName,
                                                String organizerName, BigDecimal goalAmount) {
        logger.info("==============================================");
        logger.info("SENDING CAMPAIGN CREATED NOTIFICATION TO ADMIN");
        logger.info("==============================================");
        logger.info("To: {}", ADMIN_EMAIL);
        logger.info("Subject: New Campaign Created - Review Required");
        logger.info("Body:");
        logger.info("  Dear Admin,");
        logger.info("  ");
        logger.info("  A new campaign has been created on the CareForAll platform.");
        logger.info("  ");
        logger.info("  CAMPAIGN DETAILS");
        logger.info("  ----------------");
        logger.info("  Campaign ID: {}", campaignId);
        logger.info("  Campaign Name: {}", campaignName);
        logger.info("  Organizer: {}", organizerName);
        logger.info("  Goal Amount: ${}", goalAmount);
        logger.info("  Created: {}", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logger.info("  ");
        logger.info("  Please review this campaign for approval.");
        logger.info("  ");
        logger.info("  Best regards,");
        logger.info("  CareForAll Platform");
        logger.info("==============================================");
    }

    /**
     * Send campaign completed notification to campaign organizer
     */
    public void sendCampaignCompletedNotification(String email, String organizerName,
                                                  String campaignName, BigDecimal goalAmount,
                                                  BigDecimal currentAmount, Integer totalDonations) {
        logger.info("==============================================");
        logger.info("SENDING CAMPAIGN COMPLETED NOTIFICATION");
        logger.info("==============================================");
        logger.info("To: {}", email);
        logger.info("Subject: Congratulations! Your Campaign is Complete - {}", campaignName);
        logger.info("Body:");
        logger.info("  Dear {},", organizerName);
        logger.info("  ");
        logger.info("  Congratulations! Your campaign has been successfully completed!");
        logger.info("  ");
        logger.info("  CAMPAIGN SUMMARY");
        logger.info("  ----------------");
        logger.info("  Campaign Name: {}", campaignName);
        logger.info("  Goal Amount: ${}", goalAmount);
        logger.info("  Total Raised: ${}", currentAmount);
        logger.info("  Total Donations: {}", totalDonations);

        if (currentAmount.compareTo(goalAmount) >= 0) {
            logger.info("  Status: GOAL ACHIEVED! ");
            logger.info("  ");
            logger.info("  Amazing work! You've reached and exceeded your fundraising goal!");
        } else {
            double percentage = currentAmount.divide(goalAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).doubleValue();
            logger.info("  Status: {}% of goal achieved", String.format("%.2f", percentage));
        }

        logger.info("  ");
        logger.info("  Thank you for making a difference through CareForAll.");
        logger.info("  Your campaign has helped those in need.");
        logger.info("  ");
        logger.info("  The funds will be processed and transferred to your account");
        logger.info("  within 3-5 business days.");
        logger.info("  ");
        logger.info("  Best regards,");
        logger.info("  The CareForAll Team");
        logger.info("==============================================");
    }

    /**
     * Send payment failed notification to donor
     */
    public void sendPaymentFailedNotification(String email, String donorName, String campaignName,
                                             BigDecimal amount, String paymentId, String failureReason) {
        logger.info("==============================================");
        logger.info("SENDING PAYMENT FAILED NOTIFICATION");
        logger.info("==============================================");
        logger.info("To: {}", email);
        logger.info("Subject: Payment Failed - Action Required");
        logger.info("Body:");
        logger.info("  Dear {},", donorName);
        logger.info("  ");
        logger.info("  We're sorry, but your payment could not be processed.");
        logger.info("  ");
        logger.info("  PAYMENT DETAILS");
        logger.info("  ---------------");
        logger.info("  Campaign: {}", campaignName);
        logger.info("  Amount: ${}", amount);
        logger.info("  Payment ID: {}", paymentId);
        logger.info("  Failure Reason: {}", failureReason != null ? failureReason : "Payment processing error");
        logger.info("  ");
        logger.info("  RETRY INSTRUCTIONS");
        logger.info("  ------------------");
        logger.info("  1. Verify your payment information is correct");
        logger.info("  2. Ensure you have sufficient funds in your account");
        logger.info("  3. Try a different payment method if available");
        logger.info("  4. Contact your bank if the issue persists");
        logger.info("  ");
        logger.info("  You can retry your donation by visiting the campaign page:");
        logger.info("  https://careforall.org/campaigns/{}", campaignName.toLowerCase().replaceAll(" ", "-"));
        logger.info("  ");
        logger.info("  If you continue to experience issues, please contact our support team");
        logger.info("  at support@careforall.org or call 1-800-CARE-4ALL");
        logger.info("  ");
        logger.info("  We appreciate your willingness to support this cause.");
        logger.info("  ");
        logger.info("  Best regards,");
        logger.info("  The CareForAll Team");
        logger.info("==============================================");
    }
}
