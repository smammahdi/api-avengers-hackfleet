package com.careforall.analytics.repository;

import com.careforall.analytics.model.PlatformAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformAnalyticsRepository extends MongoRepository<PlatformAnalytics, String> {
}
