package com.careforall.donation.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Create Donation Request DTO
 *
 * Request payload for creating a new donation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDonationRequest {

    @NotNull(message = "Campaign ID is required")
    private Long campaignId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Minimum donation amount is $1.00")
    @DecimalMax(value = "1000000.0", message = "Maximum donation amount is $1,000,000.00")
    private BigDecimal amount;

    @NotBlank(message = "Donor name is required")
    @Size(max = 100, message = "Donor name must not exceed 100 characters")
    private String donorName;

    @NotBlank(message = "Donor email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String donorEmail;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "CREDIT_CARD|DEBIT_CARD|PAYPAL|BANK_TRANSFER",
             message = "Payment method must be one of: CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER")
    private String paymentMethod;

    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    private Boolean isAnonymous = false;
}
