package com.careforall.analytics.repository;

import com.careforall.analytics.model.CampaignAnalytics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignAnalyticsRepository extends MongoRepository<CampaignAnalytics, String> {

    Optional<CampaignAnalytics> findByCampaignId(String campaignId);

    List<CampaignAnalytics> findByStatus(String status);

    List<CampaignAnalytics> findByOrderByTotalDonationsDesc(Pageable pageable);

    List<CampaignAnalytics> findByOrderByDonorCountDesc(Pageable pageable);
}
