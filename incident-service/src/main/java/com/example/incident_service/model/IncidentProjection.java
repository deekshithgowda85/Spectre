package com.example.incident_service.model;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public record IncidentProjection(String title, String description, String serviceName, IncidentSeverity severity,
        IncidentStatus status, Instant openedAt, Instant acknowledgedAt, Instant resolvedAt,
        Long mtta, Long mttr, String createdBy, Set<Long> linkedAlertIds) {
    public static IncidentProjection empty() {
        return new IncidentProjection(null, null, null, null, IncidentStatus.OPEN, null, null, null, null, null, null,
                new LinkedHashSet<>());
    }

    public IncidentProjection withStatus(IncidentStatus next, Instant at) {
        return new IncidentProjection(title, description, serviceName, severity, next, openedAt, acknowledgedAt,
                next == IncidentStatus.RESOLVED ? at : resolvedAt, mtta, mttr, createdBy, linkedAlertIds);
    }

    public IncidentProjection withMetrics() {
        Long nextMtta = openedAt != null && acknowledgedAt != null
                ? Duration.between(openedAt, acknowledgedAt).toMillis()
                : null;
        Long nextMttr = openedAt != null && resolvedAt != null ? Duration.between(openedAt, resolvedAt).toMillis()
                : null;
        return new IncidentProjection(title, description, serviceName, severity, status, openedAt, acknowledgedAt,
                resolvedAt, nextMtta, nextMttr, createdBy, linkedAlertIds);
    }
}
