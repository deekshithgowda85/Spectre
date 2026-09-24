package com.example.anomaly_service.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.anomaly_service.config.AnomalyProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuleEvaluationServiceTest {
    @Test
    void firesLatencyRuleAfterConsecutiveSamples() {
        AnomalyProperties properties = new AnomalyProperties();
        properties.setMonitoredServices(List.of("payments"));
        properties.setConsecutiveSamples(3);
        properties.setResponseTimeThresholdMs(BigDecimal.valueOf(1000));
        AnomalyUpstreamClient upstream = org.mockito.Mockito.mock(AnomalyUpstreamClient.class);
        when(upstream.statuses())
                .thenReturn(List.of(new AnomalyUpstreamClient.PingPoint("payments", "UP", 20, Instant.now())));
        when(upstream.recentMetrics("payments", "response_time_ms", 3))
                .thenReturn(List.of(point(1100), point(1200), point(1300)));
        when(upstream.recentMetrics("payments", "error_rate", 3)).thenReturn(List.of(point(0), point(0), point(0)));
        var observations = new RuleEvaluationService(properties, upstream).evaluate().get("payments");
        assertTrue(observations.stream().anyMatch(item -> item.type().name().equals("HIGH_LATENCY") && item.active()));
    }

    private AnomalyUpstreamClient.MetricPoint point(int value) {
        return new AnomalyUpstreamClient.MetricPoint("payments", "response_time_ms", BigDecimal.valueOf(value), "ms",
                Instant.now());
    }
}
