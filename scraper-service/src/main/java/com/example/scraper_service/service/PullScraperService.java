package com.example.scraper_service.service;

import com.example.scraper_service.config.ScraperTargetProperties;
import com.example.scraper_service.config.ScraperTargetProperties.Target;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PullScraperService {
    private final ScraperTargetProperties properties;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final MetricIngestionService ingestion;
    private final Map<String, Instant> lastPulls = new ConcurrentHashMap<>();

    public PullScraperService(ScraperTargetProperties properties, HttpClient client, ObjectMapper mapper,
            MetricIngestionService ingestion) {
        this.properties = properties;
        this.client = client;
        this.mapper = mapper;
        this.ingestion = ingestion;
    }

    @Scheduled(fixedDelayString = "${scraper.scheduler-tick-ms:1000}")
    public void pullDueTargets() {
        Instant now = Instant.now();
        properties.getTargets().stream().filter(this::valid).forEach(target -> {
            Instant last = lastPulls.get(target.getServiceName());
            if (last == null || now.toEpochMilli() - last.toEpochMilli() >= target.getIntervalMs()) {
                pull(target);
                lastPulls.put(target.getServiceName(), now);
            }
        });
    }

    void pull(Target target) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(target.getMetricsUrl()))
                    .timeout(Duration.ofMillis(properties.getHttpTimeoutMs())).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400)
                return;
            String body = response.body();
            Map<String, BigDecimal> metrics = mapper.readValue(body, new TypeReference<>() {
            });
            Instant timestamp = Instant.now();
            metrics.forEach(
                    (name, value) -> ingestion.save(target.getServiceName(), name, value, "unknown", timestamp));
        } catch (Exception ignored) {
        }
    }

    private boolean valid(Target target) {
        return target.getServiceName() != null && !target.getServiceName().isBlank() && target.getMetricsUrl() != null
                && !target.getMetricsUrl().isBlank();
    }
}
