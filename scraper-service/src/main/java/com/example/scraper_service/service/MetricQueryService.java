package com.example.scraper_service.service;

import com.example.scraper_service.dto.MetricDtos.LatestResponse;
import com.example.scraper_service.dto.MetricDtos.Response;
import com.example.scraper_service.repository.MetricSampleRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricQueryService {
    private final MetricSampleRepository samples;

    public MetricQueryService(MetricSampleRepository samples) {
        this.samples = samples;
    }

    @Transactional(readOnly = true)
    public Page<Response> search(String service, String metric, Instant from, Instant to, Pageable pageable) {
        return samples.search(blankToNull(service), blankToNull(metric), from, to, pageable).map(Response::from);
    }

    @Transactional(readOnly = true)
    public LatestResponse latest(String service, Pageable pageable) {
        List<Response> ordered = samples.findByServiceNameOrderByTimestampDesc(service, pageable).stream()
                .map(Response::from).toList();
        var latest = new LinkedHashMap<String, Response>();
        ordered.forEach(sample -> latest.putIfAbsent(sample.metricName(), sample));
        return new LatestResponse(service, List.copyOf(latest.values()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
