package com.example.scraper_service.repository;

import com.example.scraper_service.model.MetricSample;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MetricSampleRepository extends JpaRepository<MetricSample, Long> {
    @Query("select sample from MetricSample sample where (:serviceName is null or sample.serviceName = :serviceName) and (:metricName is null or sample.metricName = :metricName) and sample.timestamp between :from and :to order by sample.timestamp asc")
    Page<MetricSample> search(@Param("serviceName") String serviceName, @Param("metricName") String metricName,
            @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    List<MetricSample> findByServiceNameOrderByTimestampDesc(String serviceName, Pageable pageable);

    Optional<MetricSample> findTopByServiceNameAndMetricNameOrderByTimestampDesc(String serviceName, String metricName);
}
