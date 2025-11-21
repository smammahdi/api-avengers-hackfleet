package com.careforall.campaign.service;

import com.careforall.campaign.dto.CampaignRequest;
import com.careforall.campaign.dto.CampaignResponse;
import com.careforall.campaign.entity.Campaign;
import com.careforall.campaign.entity.CampaignStatus;
import com.careforall.campaign.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CampaignService using Mockito
 */
@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CampaignService campaignService;

    private Campaign testCampaign;
    private CampaignRequest campaignRequest;

    @BeforeEach
    void setUp() {
        testCampaign = new Campaign();
        testCampaign.setId(1L);
        testCampaign.setName("Test Campaign");
        testCampaign.setDescription("Test Description for fundraising");
        testCampaign.setGoalAmount(new BigDecimal("10000.00"));
        testCampaign.setCurrentAmount(new BigDecimal("5000.00"));
        testCampaign.setCategory("Medical");
        testCampaign.setOrganizerName("John Doe");
        testCampaign.setOrganizerEmail("john@example.com");
        testCampaign.setStartDate(LocalDate.now());
        testCampaign.setEndDate(LocalDate.now().plusDays(30));
        testCampaign.setStatus(CampaignStatus.ACTIVE);
        testCampaign.setImageUrl("http://example.com/image.jpg");

        campaignRequest = new CampaignRequest();
        campaignRequest.setName("Test Campaign");
        campaignRequest.setDescription("Test Description for fundraising");
        campaignRequest.setGoalAmount(new BigDecimal("10000.00"));
        campaignRequest.setCategory("Medical");
        campaignRequest.setOrganizerName("John Doe");
        campaignRequest.setOrganizerEmail("john@example.com");
        campaignRequest.setStartDate(LocalDate.now());
        campaignRequest.setEndDate(LocalDate.now().plusDays(30));
        campaignRequest.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    void testGetAllCampaigns_Success() {
        // Arrange
        List<Campaign> campaigns = Arrays.asList(testCampaign, createCampaign(2L, "Campaign 2"));
        when(campaignRepository.findByStatus(CampaignStatus.ACTIVE)).thenReturn(campaigns);

        // Act
        List<CampaignResponse> response = campaignService.getAllCampaigns();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Test Campaign", response.get(0).getName());
        verify(campaignRepository, times(1)).findByStatus(CampaignStatus.ACTIVE);
    }

    @Test
    void testGetAllCampaigns_EmptyList() {
        // Arrange
        when(campaignRepository.findByStatus(CampaignStatus.ACTIVE)).thenReturn(Collections.emptyList());

        // Act
        List<CampaignResponse> response = campaignService.getAllCampaigns();

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(campaignRepository, times(1)).findByStatus(CampaignStatus.ACTIVE);
    }

    @Test
    void testGetCampaignById_Success() {
        // Arrange
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        // Act
        CampaignResponse response = campaignService.getCampaignById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(testCampaign.getId(), response.getId());
        assertEquals(testCampaign.getName(), response.getName());
        assertEquals(testCampaign.getGoalAmount(), response.getGoalAmount());
        verify(campaignRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCampaignById_NotFound() {
        // Arrange
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            campaignService.getCampaignById(999L);
        });

        assertTrue(exception.getMessage().contains("Campaign not found"));
        verify(campaignRepository, times(1)).findById(999L);
    }

    @Test
    void testGetCampaignsByCategory_Success() {
        // Arrange
        List<Campaign> campaigns = Arrays.asList(testCampaign);
        when(campaignRepository.findByCategoryAndStatus("Medical", CampaignStatus.ACTIVE))
            .thenReturn(campaigns);

        // Act
        List<CampaignResponse> response = campaignService.getCampaignsByCategory("Medical");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Medical", response.get(0).getCategory());
        verify(campaignRepository, times(1)).findByCategoryAndStatus("Medical", CampaignStatus.ACTIVE);
    }

    @Test
    void testGetCampaignsByCategory_NoResults() {
        // Arrange
        when(campaignRepository.findByCategoryAndStatus("NonExistent", CampaignStatus.ACTIVE))
            .thenReturn(Collections.emptyList());

        // Act
        List<CampaignResponse> response = campaignService.getCampaignsByCategory("NonExistent");

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(campaignRepository, times(1)).findByCategoryAndStatus("NonExistent", CampaignStatus.ACTIVE);
    }

    @Test
    void testSearchCampaigns_Success() {
        // Arrange
        List<Campaign> campaigns = Arrays.asList(testCampaign);
        when(campaignRepository.findByNameContainingIgnoreCaseAndStatus("Test", CampaignStatus.ACTIVE))
            .thenReturn(campaigns);

        // Act
        List<CampaignResponse> response = campaignService.searchCampaigns("Test");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        assertTrue(response.get(0).getName().contains("Test"));
        verify(campaignRepository, times(1)).findByNameContainingIgnoreCaseAndStatus("Test", CampaignStatus.ACTIVE);
    }

    @Test
    void testSearchCampaigns_NoResults() {
        // Arrange
        when(campaignRepository.findByNameContainingIgnoreCaseAndStatus("NonExistent", CampaignStatus.ACTIVE))
            .thenReturn(Collections.emptyList());

        // Act
        List<CampaignResponse> response = campaignService.searchCampaigns("NonExistent");

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(campaignRepository, times(1)).findByNameContainingIgnoreCaseAndStatus("NonExistent", CampaignStatus.ACTIVE);
    }

    @Test
    void testGetCampaignsByOrganizer_Success() {
        // Arrange
        List<Campaign> campaigns = Arrays.asList(testCampaign);
        when(campaignRepository.findByOrganizerEmail("john@example.com")).thenReturn(campaigns);

        // Act
        List<CampaignResponse> response = campaignService.getCampaignsByOrganizer("john@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("john@example.com", response.get(0).getOrganizerEmail());
        verify(campaignRepository, times(1)).findByOrganizerEmail("john@example.com");
    }

    @Test
    void testCreateCampaign_Success() {
        // Arrange
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        // Act
        CampaignResponse response = campaignService.createCampaign(campaignRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testCampaign.getName(), response.getName());
        assertEquals(testCampaign.getGoalAmount(), response.getGoalAmount());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), any(Object.class));
    }

    @Test
    void testCreateCampaign_InvalidDates() {
        // Arrange
        campaignRequest.setEndDate(LocalDate.now().minusDays(1)); // End date before start date

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            campaignService.createCampaign(campaignRequest);
        });

        assertTrue(exception.getMessage().contains("End date must be after start date"));
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void testUpdateCampaign_Success() {
        // Arrange
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignRequest.setName("Updated Campaign");
        campaignRequest.setGoalAmount(new BigDecimal("15000.00"));

        // Act
        CampaignResponse response = campaignService.updateCampaign(1L, campaignRequest);

        // Assert
        assertNotNull(response);
        verify(campaignRepository, times(1)).findById(1L);
        verify(campaignRepository, times(1)).save(any(Campaign.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testUpdateCampaign_NotFound() {
        // Arrange
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            campaignService.updateCampaign(999L, campaignRequest);
        });

        assertTrue(exception.getMessage().contains("Campaign not found"));
        verify(campaignRepository, times(1)).findById(999L);
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void testCancelCampaign_Success() {
        // Arrange
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        // Act
        campaignService.cancelCampaign(1L);

        // Assert
        verify(campaignRepository, times(1)).findById(1L);
        verify(campaignRepository, times(1)).save(any(Campaign.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testCancelCampaign_NotFound() {
        // Arrange
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            campaignService.cancelCampaign(999L);
        });

        assertTrue(exception.getMessage().contains("Campaign not found"));
        verify(campaignRepository, times(1)).findById(999L);
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void testUpdateDonationAmount_Success() {
        // Arrange
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        BigDecimal donationAmount = new BigDecimal("1000.00");

        // Act
        CampaignResponse response = campaignService.updateDonationAmount(1L, donationAmount);

        // Assert
        assertNotNull(response);
        verify(campaignRepository, times(1)).findById(1L);
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    void testUpdateDonationAmount_InactiveCampaign() {
        // Arrange
        testCampaign.setStatus(CampaignStatus.CANCELLED);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        BigDecimal donationAmount = new BigDecimal("1000.00");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            campaignService.updateDonationAmount(1L, donationAmount);
        });

        assertTrue(exception.getMessage().contains("Cannot donate to inactive campaign"));
        verify(campaignRepository, times(1)).findById(1L);
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void testUpdateDonationAmount_GoalReached() {
        // Arrange
        testCampaign.setCurrentAmount(new BigDecimal("9500.00")); // Close to goal
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        BigDecimal donationAmount = new BigDecimal("1000.00"); // This will exceed goal

        // Act
        CampaignResponse response = campaignService.updateDonationAmount(1L, donationAmount);

        // Assert
        assertNotNull(response);
        assertTrue(response.isGoalReached());
        verify(campaignRepository, times(1)).findById(1L);
        verify(campaignRepository, times(1)).save(any(Campaign.class));
        // Should publish goal reached event
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    // Helper method to create test campaigns
    private Campaign createCampaign(Long id, String name) {
        Campaign campaign = new Campaign();
        campaign.setId(id);
        campaign.setName(name);
        campaign.setDescription("Description for " + name);
        campaign.setGoalAmount(new BigDecimal("5000.00"));
        campaign.setCurrentAmount(new BigDecimal("1000.00"));
        campaign.setCategory("Education");
        campaign.setOrganizerName("Jane Smith");
        campaign.setOrganizerEmail("jane@example.com");
        campaign.setStartDate(LocalDate.now());
        campaign.setEndDate(LocalDate.now().plusDays(30));
        campaign.setStatus(CampaignStatus.ACTIVE);
        return campaign;
    }
}
