package com.example.scraper_service.service;

import com.example.scraper_service.dto.MetricDtos.IngestRequest;
import com.example.scraper_service.dto.MetricDtos.MetricInput;
import com.example.scraper_service.dto.MetricDtos.Response;
import com.example.scraper_service.model.MetricSample;
import com.example.scraper_service.repository.MetricSampleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricIngestionService {
    private final MetricSampleRepository samples;

    public MetricIngestionService(MetricSampleRepository samples) {
        this.samples = samples;
    }

    @Transactional
    public List<Response> ingest(IngestRequest request) {
        Instant timestamp = request.timestamp() == null ? Instant.now() : request.timestamp();
        List<MetricSample> saved = request
                .metrics().stream().map(metric -> new MetricSample(request.serviceName().trim(),
                        metric.metricName().trim(), metric.value(), metric.unit().trim(), timestamp))
                .map(samples::save).toList();
        return saved.stream().map(Response::from).toList();
    }

    @Transactional
    public Response save(String serviceName, String metricName, BigDecimal value, String unit, Instant timestamp) {
        return Response.from(samples.save(new MetricSample(serviceName, metricName, value, unit, timestamp)));
    }
}
