package com.example.incident_service.service;

import com.example.incident_service.model.IncidentProjection;
import java.time.Duration;
import java.time.Instant;

public final class IncidentCorrelationPolicy {
    private IncidentCorrelationPolicy() {
    }

    public static boolean shouldMerge(IncidentProjection incident, String serviceName, Instant alertTime,
            long windowMinutes) {
        return incident.serviceName().equals(serviceName)
                && incident.status() != com.example.incident_service.model.IncidentStatus.RESOLVED
                && !alertTime.isBefore(incident.openedAt())
                && !alertTime.isAfter(incident.openedAt().plus(Duration.ofMinutes(windowMinutes)));
    }
}
