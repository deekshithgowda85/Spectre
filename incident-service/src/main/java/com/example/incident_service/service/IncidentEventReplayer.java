package com.example.incident_service.service;

import com.example.incident_service.model.IncidentEvent;
import com.example.incident_service.model.IncidentEventType;
import com.example.incident_service.model.IncidentProjection;
import com.example.incident_service.model.IncidentSeverity;
import com.example.incident_service.model.IncidentStatus;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class IncidentEventReplayer {
    public IncidentProjection replay(List<IncidentEvent> events) {
        IncidentProjection state = IncidentProjection.empty();
        for (IncidentEvent event : events.stream().sorted(Comparator.comparing(IncidentEvent::getCreatedAt)).toList())
            state = apply(state, event);
        return state.withMetrics();
    }

    private IncidentProjection apply(IncidentProjection state, IncidentEvent event) {
        Map<String, Object> payload = event.getPayload();
        if (event.getEventType() == IncidentEventType.CREATED)
            return new IncidentProjection(string(payload, "title"), string(payload, "description"),
                    string(payload, "serviceName"), IncidentSeverity.valueOf(string(payload, "severity")),
                    IncidentStatus.OPEN, event.getCreatedAt(), null, null, null, null, event.getCreatedBy(),
                    new LinkedHashSet<>());
        LinkedHashSet<Long> links = new LinkedHashSet<>(state.linkedAlertIds());
        if (event.getEventType() == IncidentEventType.ALERT_LINKED)
            links.add(number(payload, "alertId"));
        IncidentProjection next = new IncidentProjection(state.title(), state.description(), state.serviceName(),
                state.severity(), state.status(), state.openedAt(), state.acknowledgedAt(), state.resolvedAt(),
                state.mtta(), state.mttr(), state.createdBy(), links);
        if (event.getEventType() == IncidentEventType.ACKNOWLEDGED
                || event.getEventType() == IncidentEventType.ESCALATED)
            next = new IncidentProjection(next.title(), next.description(), next.serviceName(), next.severity(),
                    IncidentStatus.ACKNOWLEDGED, next.openedAt(),
                    event.getEventType() == IncidentEventType.ACKNOWLEDGED ? event.getCreatedAt()
                            : next.acknowledgedAt(),
                    next.resolvedAt(), next.mtta(), next.mttr(), next.createdBy(), links);
        if (event.getEventType() == IncidentEventType.RESOLVED)
            next = next.withStatus(IncidentStatus.RESOLVED, event.getCreatedAt());
        if (event.getEventType() == IncidentEventType.REOPENED)
            next = new IncidentProjection(next.title(), next.description(), next.serviceName(), next.severity(),
                    IncidentStatus.OPEN, next.openedAt(), null, null, null, null, next.createdBy(), links);
        return next;
    }

    private String string(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null)
            throw new IllegalArgumentException("Missing event payload: " + key);
        return value.toString();
    }

    private Long number(Map<String, Object> payload, String key) {
        return Long.valueOf(string(payload, key));
    }
}
