package com.example.anomaly_service.dto;

import com.example.anomaly_service.model.Alert;
import com.example.anomaly_service.model.AlertSeverity;
import com.example.anomaly_service.model.AlertType;
import java.time.Instant;

public final class AlertDtos {
    private AlertDtos() {
    }

    public record Response(Long id, String serviceName, AlertType type, AlertSeverity severity, String message,
            String sourceMetric, Instant triggeredAt, Instant resolvedAt) {
        public static Response from(Alert alert) {
            return new Response(alert.getId(), alert.getServiceName(), alert.getType(), alert.getSeverity(),
                    alert.getMessage(), alert.getSourceMetric(), alert.getTriggeredAt(), alert.getResolvedAt());
        }
    }
}
