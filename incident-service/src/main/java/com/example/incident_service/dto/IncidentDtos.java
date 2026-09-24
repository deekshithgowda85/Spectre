package com.example.incident_service.dto;

import com.example.incident_service.model.Incident;
import com.example.incident_service.model.IncidentSeverity;
import com.example.incident_service.model.IncidentStatus;
import com.example.incident_service.model.IncidentEvent;
import com.example.incident_service.model.IncidentProjection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class IncidentDtos {
    private IncidentDtos() {
    }

    public record CreateRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 5000) String description,
            @NotBlank @Size(max = 120) String serviceName,
            @NotNull IncidentSeverity severity) {
    }

    public record EventResponse(Long id, Long incidentId, String eventType, Object payload, Instant createdAt,
            String createdBy) {
        public static EventResponse from(IncidentEvent event) {
            return new EventResponse(event.getId(), event.getIncidentId(), event.getEventType().name(),
                    event.getPayload(), event.getCreatedAt(), event.getCreatedBy());
        }
    }

    public record Response(Long id, String title, String description, String serviceName, IncidentSeverity severity,
            IncidentStatus status, Instant createdAt, Instant openedAt, Instant updatedAt,
            Instant acknowledgedAt, Instant resolvedAt, Long mtta, Long mttr, String createdBy,
            Set<Long> linkedAlertIds, List<EventResponse> events) {
        public static Response from(Incident incident, IncidentProjection projection, List<IncidentEvent> events) {
            return new Response(incident.getId(), projection.title(), projection.description(),
                    projection.serviceName(), projection.severity(), projection.status(), projection.openedAt(),
                    projection.openedAt(), incident.getUpdatedAt(), projection.acknowledgedAt(),
                    projection.resolvedAt(), projection.mtta(), projection.mttr(), projection.createdBy(),
                    projection.linkedAlertIds(), events.stream().map(EventResponse::from).toList());
        }
    }
}
