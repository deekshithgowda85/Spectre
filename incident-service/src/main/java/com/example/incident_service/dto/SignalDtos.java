package com.example.incident_service.dto;

import java.time.Instant;
import java.util.List;

public final class SignalDtos {
    private SignalDtos() {
    }

    public record Response(List<Alert> alerts, List<ServiceHealth> services, List<Metric> metrics) {
    }

    public record Alert(String id, String source, String service, String severity, String message,
            Instant triggeredAt, String incidentId) {
    }

    public record ServiceHealth(String name, String health, long openIncidents, long totalIncidents) {
    }

    public record Metric(String name, double value, String unit, Instant measuredAt) {
    }
}
