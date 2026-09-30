package com.example.dashboard_service.dto;

import com.example.dashboard_service.model.AuditEvent;
import java.time.Instant;

public final class AuditEventDtos {
    private AuditEventDtos() {
    }

    public record Response(Long id, String actor, String action, String resource,
            Instant occurredAt, String ipAddress) {
        public static Response from(AuditEvent event) {
            return new Response(event.getId(), event.getActor(), event.getAction().name(),
                    event.getResource(), event.getOccurredAt(), event.getIpAddress());
        }
    }
}