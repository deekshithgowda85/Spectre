package com.example.anomaly_service.service;

import com.example.anomaly_service.config.AnomalyProperties;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AnomalyUpstreamClient {
    private final RestClient scraper;
    private final RestClient ping;
    private final AnomalyProperties properties;

    public AnomalyUpstreamClient(RestClient.Builder builder, AnomalyProperties properties) {
        this.properties = properties;
        scraper = builder.baseUrl(properties.getScraperUrl()).build();
        ping = builder.baseUrl(properties.getPingUrl()).build();
    }

    public List<MetricPoint> recentMetrics(String service, String metric, int size) {
        return scraper.get()
                .uri(uri -> uri.path("/api/scraper/metrics").queryParam("service", service).queryParam("metric", metric)
                        .queryParam("size", size).build())
                .headers(this::auth).retrieve().body(new ParameterizedTypeReference<PageResponse<MetricPoint>>() {
                }).content();
    }

    public List<PingPoint> statuses() {
        return ping.get().uri("/api/ping/status").headers(this::auth).retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private void auth(HttpHeaders headers) {
        if (properties.getServiceToken() != null && !properties.getServiceToken().isBlank())
            headers.setBearerAuth(properties.getServiceToken());
    }

    public record PageResponse<T>(List<T> content) {
    }

    public record MetricPoint(String serviceName, String metricName, BigDecimal value, String unit,
            java.time.Instant timestamp) {
    }

    public record PingPoint(String targetName, String status, long latencyMs, java.time.Instant checkedAt) {
    }
}
