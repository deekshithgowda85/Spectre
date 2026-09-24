package com.example.anomaly_service.service;

import com.example.anomaly_service.config.AnomalyProperties;
import com.example.anomaly_service.model.AlertSeverity;
import com.example.anomaly_service.model.AlertType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RuleEvaluationService {
    private final AnomalyProperties properties;
    private final AnomalyUpstreamClient upstream;

    public RuleEvaluationService(AnomalyProperties properties, AnomalyUpstreamClient upstream) {
        this.properties = properties;
        this.upstream = upstream;
    }

    public Map<String, List<RuleObservation>> evaluate() {
        Map<String, List<RuleObservation>> result = new LinkedHashMap<>();
        for (String service : properties.getMonitoredServices()) {
            try {
                result.put(service, evaluateService(service));
            } catch (RuntimeException ignored) {
            }
        }
        return result;
    }

    private List<RuleObservation> evaluateService(String service) {
        Instant now = Instant.now();
        List<RuleObservation> observations = new ArrayList<>();
        boolean down = upstream.statuses().stream().filter(status -> status.targetName().equals(service)).findFirst()
                .map(status -> "DOWN".equals(status.status())).orElse(false);
        observations.add(new RuleObservation(service, AlertType.SERVICE_DOWN, AlertSeverity.CRITICAL,
                service + " is down", "service_status", down, now));
        List<AnomalyUpstreamClient.MetricPoint> latency = upstream.recentMetrics(service, "response_time_ms",
                properties.getConsecutiveSamples());
        boolean latencyHigh = consecutiveAbove(latency.stream().map(AnomalyUpstreamClient.MetricPoint::value).toList(),
                properties.getResponseTimeThresholdMs());
        observations.add(new RuleObservation(service, AlertType.HIGH_LATENCY, AlertSeverity.HIGH,
                service + " response latency is above threshold", "response_time_ms", latencyHigh,
                latest(latency, now)));
        List<AnomalyUpstreamClient.MetricPoint> errors = upstream.recentMetrics(service, "error_rate",
                properties.getConsecutiveSamples());
        boolean errorSpike = consecutiveAbove(errors.stream().map(AnomalyUpstreamClient.MetricPoint::value).toList(),
                properties.getErrorRateThreshold());
        observations.add(new RuleObservation(service, AlertType.ERROR_RATE_SPIKE, AlertSeverity.HIGH,
                service + " error rate is above threshold", "error_rate", errorSpike, latest(errors, now)));
        return observations;
    }

    private boolean consecutiveAbove(List<BigDecimal> values, BigDecimal threshold) {
        if (values.size() < properties.getConsecutiveSamples())
            return false;
        return values.subList(Math.max(0, values.size() - properties.getConsecutiveSamples()), values.size()).stream()
                .allMatch(value -> value.compareTo(threshold) > 0);
    }

    private Instant latest(List<AnomalyUpstreamClient.MetricPoint> values, Instant fallback) {
        return values.isEmpty() ? fallback : values.get(values.size() - 1).timestamp();
    }
}
