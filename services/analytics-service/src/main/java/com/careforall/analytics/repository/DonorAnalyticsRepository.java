package com.careforall.analytics.repository;

import com.careforall.analytics.model.DonorAnalytics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorAnalyticsRepository extends MongoRepository<DonorAnalytics, String> {

    Optional<DonorAnalytics> findByUserId(String userId);

    Optional<DonorAnalytics> findByDonorEmail(String email);

    List<DonorAnalytics> findByOrderByTotalDonatedDesc(Pageable pageable);

    Long countByDonationCountGreaterThan(Integer count);
}
