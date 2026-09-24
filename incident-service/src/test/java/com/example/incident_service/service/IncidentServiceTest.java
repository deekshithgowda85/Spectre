package com.example.incident_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.incident_service.config.IncidentProperties;
import com.example.incident_service.model.IncidentEvent;
import com.example.incident_service.model.IncidentEventType;
import com.example.incident_service.model.IncidentProjection;
import com.example.incident_service.model.IncidentSeverity;
import com.example.incident_service.model.IncidentStatus;
import com.example.incident_service.repository.IncidentEventRepository;
import com.example.incident_service.repository.IncidentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IncidentServiceTest {
    @Test
    void replayReconstructsStateAndMetrics() {
        Instant opened = Instant.parse("2026-01-01T00:00:00Z");
        IncidentEventReplayer replayer = new IncidentEventReplayer();
        List<IncidentEvent> events = List.of(
                new IncidentEvent(1L, IncidentEventType.CREATED,
                        Map.of("title", "Outage", "description", "Down", "serviceName", "payments", "severity",
                                "CRITICAL"),
                        opened, "operator"),
                new IncidentEvent(1L, IncidentEventType.ALERT_LINKED, Map.of("alertId", 7L), opened.plusSeconds(10),
                        "anomaly-service"),
                new IncidentEvent(1L, IncidentEventType.ACKNOWLEDGED, Map.of(), opened.plusSeconds(60), "operator"),
                new IncidentEvent(1L, IncidentEventType.RESOLVED, Map.of(), opened.plusSeconds(360), "operator"));
        IncidentProjection state = replayer.replay(events);
        assertEquals(IncidentStatus.RESOLVED, state.status());
        assertEquals(60000L, state.mtta());
        assertEquals(360000L, state.mttr());
        assertTrue(state.linkedAlertIds().contains(7L));
    }

    @Test
    void correlationOnlyMergesSameServiceWithinWindow() {
        IncidentProjection state = new IncidentProjection("Outage", "Down", "payments", IncidentSeverity.HIGH,
                IncidentStatus.OPEN, Instant.parse("2026-01-01T00:00:00Z"), null, null, null, null, "operator",
                java.util.Set.of());
        assertTrue(IncidentCorrelationPolicy.shouldMerge(state, "payments", Instant.parse("2026-01-01T00:10:00Z"), 30));
        assertTrue(!IncidentCorrelationPolicy.shouldMerge(state, "billing", Instant.parse("2026-01-01T00:10:00Z"), 30));
        assertTrue(
                !IncidentCorrelationPolicy.shouldMerge(state, "payments", Instant.parse("2026-01-01T01:00:00Z"), 30));
    }

    @Test
    void rejectsResolveFromOpenState() {
        IncidentEventRepository events = Mockito.mock(IncidentEventRepository.class);
        IncidentRepository incidents = Mockito.mock(IncidentRepository.class);
        IncidentProperties properties = new IncidentProperties();
        AnomalyAlertClient anomaly = Mockito.mock(AnomalyAlertClient.class);
        IncidentEventReplayer replayer = new IncidentEventReplayer();
        when(events.findByIncidentIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(new IncidentEvent(1L, IncidentEventType.CREATED,
                        Map.of("title", "Outage", "description", "Down", "serviceName", "payments", "severity", "HIGH"),
                        Instant.now(), "operator")));
        IncidentService service = new IncidentService(incidents, events, replayer, properties, anomaly);
        assertThrows(IllegalStateException.class, () -> service.resolve(1L, "operator"));
    }
}
