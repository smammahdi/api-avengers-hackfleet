package com.careforall.campaign.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Campaign Request DTO
 *
 * Used for creating and updating campaigns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRequest {

    @NotBlank(message = "Campaign name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Goal amount is required")
    @DecimalMin(value = "1.0", message = "Goal amount must be at least 1.0")
    private BigDecimal goalAmount;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Organizer name is required")
    private String organizerName;

    @NotBlank(message = "Organizer email is required")
    @Email(message = "Organizer email must be valid")
    private String organizerEmail;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String imageUrl;
}
