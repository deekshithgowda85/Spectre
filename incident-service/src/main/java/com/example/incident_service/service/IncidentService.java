package com.example.incident_service.service;

import com.example.incident_service.config.IncidentProperties;
import com.example.incident_service.dto.IncidentDtos.CreateRequest;
import com.example.incident_service.dto.IncidentDtos.Response;
import com.example.incident_service.model.*;
import com.example.incident_service.repository.IncidentEventRepository;
import com.example.incident_service.repository.IncidentRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {
    private final IncidentRepository incidents;
    private final IncidentEventRepository events;
    private final IncidentEventReplayer replayer;
    private final IncidentProperties properties;
    private final AnomalyAlertClient anomaly;

    public IncidentService(IncidentRepository incidents, IncidentEventRepository events, IncidentEventReplayer replayer,
            IncidentProperties properties, AnomalyAlertClient anomaly) {
        this.incidents = incidents;
        this.events = events;
        this.replayer = replayer;
        this.properties = properties;
        this.anomaly = anomaly;
    }

    @Transactional(readOnly = true)
    public List<Response> list(IncidentSeverity severity, IncidentStatus status) {
        return incidents.findAllByOrderByOpenedAtDesc().stream().map(item -> response(item.getId(), false))
                .filter(item -> severity == null || item.severity() == severity)
                .filter(item -> status == null || item.status() == status).toList();
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return response(id, true);
    }

    @Transactional
    public Response create(CreateRequest request, String actor) {
        Incident incident = incidents.save(new Incident(request.title().trim(), request.description().trim(),
                request.serviceName().trim(), request.severity(), actor));
        append(incident.getId(), IncidentEventType.CREATED, createdPayload(request), actor);
        return response(incident.getId(), true);
    }

    @Transactional
    public Response acknowledge(Long id, String actor) {
        require(state(id).status() == IncidentStatus.OPEN, "Only open incidents can be acknowledged");
        append(id, IncidentEventType.ACKNOWLEDGED, Map.of(), actor);
        return response(id, true);
    }

    @Transactional
    public Response resolve(Long id, String actor) {
        require(state(id).status() == IncidentStatus.ACKNOWLEDGED, "Only acknowledged incidents can be resolved");
        append(id, IncidentEventType.RESOLVED, Map.of(), actor);
        return response(id, true);
    }

    @Transactional
    public Response reopen(Long id, String actor) {
        require(state(id).status() == IncidentStatus.RESOLVED, "Only resolved incidents can be reopened");
        append(id, IncidentEventType.REOPENED, Map.of(), actor);
        return response(id, true);
    }

    @Transactional(readOnly = true)
    public List<IncidentEvent> eventStream(Long id) {
        requireIncident(id);
        return events.findByIncidentIdOrderByCreatedAtAsc(id);
    }

    @Transactional(readOnly = true)
    public double average(String metric, Instant from, Instant to, String service) {
        return incidents.findAllByOrderByOpenedAtDesc().stream().map(item -> state(item.getId()))
                .filter(item -> service == null || service.equals(item.serviceName()))
                .filter(item -> !item.openedAt().isBefore(from) && !item.openedAt().isAfter(to))
                .mapToLong(item -> "mtta".equals(metric) ? Optional.ofNullable(item.mtta()).orElse(0L)
                        : Optional.ofNullable(item.mttr()).orElse(0L))
                .average().orElse(0) / 60000d;
    }

    @Scheduled(fixedDelayString = "${incident.scheduler-tick-ms:10000}")
    @Transactional
    public void correlateAlerts() {
        try {
            List<AnomalyAlertClient.AlertRecord> open = anomaly.list("OPEN");
            for (AnomalyAlertClient.AlertRecord alert : open)
                correlate(alert);
            resolveCleared(open);
        } catch (RuntimeException ignored) {
        }
    }

    private void correlate(AnomalyAlertClient.AlertRecord alert) {
        if (linkedIncident(alert.id()).isPresent())
            return;
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(properties.getCorrelationWindowMinutes()));
        Optional<Incident> existing = incidents.findFirstByServiceNameAndStatusAndOpenedAtAfterOrderByOpenedAtDesc(
                alert.serviceName(), IncidentStatus.OPEN, cutoff)
                .filter(incident -> IncidentCorrelationPolicy.shouldMerge(state(incident.getId()), alert.serviceName(),
                        alert.triggeredAt(), properties.getCorrelationWindowMinutes()));
        if (existing.isEmpty()) {
            Incident incident = incidents.save(new Incident(alert.message(), alert.message(), alert.serviceName(),
                    severity(alert.severity()), "anomaly-service"));
            append(incident.getId(), IncidentEventType.CREATED, Map.of("title", alert.message(), "description",
                    alert.message(), "serviceName", alert.serviceName(), "severity", severity(alert.severity()).name()),
                    "anomaly-service");
            existing = Optional.of(incident);
        }
        append(existing.get().getId(), IncidentEventType.ALERT_LINKED, Map.of("alertId", alert.id()),
                "anomaly-service");
    }

    private void resolveCleared(List<AnomalyAlertClient.AlertRecord> openAlerts) {
        Set<Long> openIds = openAlerts.stream().map(AnomalyAlertClient.AlertRecord::id)
                .collect(java.util.stream.Collectors.toSet());
        for (Incident incident : incidents.findByStatus(IncidentStatus.OPEN)) {
            IncidentProjection state = state(incident.getId());
            if (!state.linkedAlertIds().isEmpty() && state.linkedAlertIds().stream().noneMatch(openIds::contains))
                append(incident.getId(), IncidentEventType.RESOLVED, Map.of("reason", "all linked alerts resolved"),
                        "anomaly-service");
        }
    }

    private Optional<Long> linkedIncident(Long alertId) {
        return events.findAll().stream()
                .filter(event -> event.getEventType() == IncidentEventType.ALERT_LINKED
                        && String.valueOf(event.getPayload().get("alertId")).equals(String.valueOf(alertId)))
                .map(IncidentEvent::getIncidentId).findFirst();
    }

    private void append(Long id, IncidentEventType type, Map<String, Object> payload, String actor) {
        events.save(new IncidentEvent(id, type, payload, Instant.now(), actor));
        Incident incident = requireIncident(id);
        incident.apply(state(id));
        incidents.save(incident);
    }

    private IncidentProjection state(Long id) {
        return replayer.replay(events.findByIncidentIdOrderByCreatedAtAsc(id));
    }

    private Response response(Long id, boolean includeEvents) {
        Incident incident = requireIncident(id);
        List<IncidentEvent> stream = events.findByIncidentIdOrderByCreatedAtAsc(id);
        return Response.from(incident, replayer.replay(stream), includeEvents ? stream : List.of());
    }

    private Incident requireIncident(Long id) {
        return incidents.findById(id).orElseThrow(() -> new IllegalArgumentException("Incident not found"));
    }

    private void require(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }

    private Map<String, Object> createdPayload(CreateRequest request) {
        return Map.of("title", request.title().trim(), "description", request.description().trim(), "serviceName",
                request.serviceName().trim(), "severity", request.severity().name());
    }

    private IncidentSeverity severity(String value) {
        try {
            return IncidentSeverity.valueOf(value);
        } catch (Exception ignored) {
            return IncidentSeverity.HIGH;
        }
    }
}
