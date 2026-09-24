package com.example.anomaly_service.service;

import com.example.anomaly_service.model.AlertSeverity;
import com.example.anomaly_service.model.AlertType;
import java.time.Instant;

public record RuleObservation(String serviceName, AlertType type, AlertSeverity severity, String message,
        String sourceMetric, boolean active, Instant observedAt) {
}
