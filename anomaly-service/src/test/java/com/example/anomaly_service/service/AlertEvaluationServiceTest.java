package com.example.anomaly_service.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.anomaly_service.model.Alert;
import com.example.anomaly_service.model.AlertEvent;
import com.example.anomaly_service.model.AlertSeverity;
import com.example.anomaly_service.model.AlertType;
import com.example.anomaly_service.repository.AlertEventRepository;
import com.example.anomaly_service.repository.AlertRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertEvaluationServiceTest {
    @Mock
    private RuleEvaluationService rules;
    @Mock
    private AlertRepository alerts;
    @Mock
    private AlertEventRepository events;
    @InjectMocks
    private AlertEvaluationService service;

    @Test
    void deduplicatesOngoingAlert() {
        RuleObservation observation = new RuleObservation("payments", AlertType.HIGH_LATENCY, AlertSeverity.HIGH,
                "slow", "response_time_ms", true, Instant.now());
        Alert alert = new Alert("payments", AlertType.HIGH_LATENCY, AlertSeverity.HIGH, "slow", "response_time_ms",
                Instant.now());
        when(rules.evaluate()).thenReturn(Map.of("payments", List.of(observation)));
        when(alerts.findByServiceNameAndTypeAndResolvedAtIsNull("payments", AlertType.HIGH_LATENCY))
                .thenReturn(Optional.empty(), Optional.of(alert));
        when(alerts.save(org.mockito.ArgumentMatchers.any(Alert.class))).thenReturn(alert);
        service.evaluateNow();
        service.evaluateNow();
        verify(alerts, times(1)).save(org.mockito.ArgumentMatchers.any(Alert.class));
        verify(events, times(1)).save(org.mockito.ArgumentMatchers.any(AlertEvent.class));
    }

    @Test
    void resolvesAlertWhenConditionClears() {
        Alert alert = new Alert("payments", AlertType.SERVICE_DOWN, AlertSeverity.CRITICAL, "down", "service_status",
                Instant.now());
        RuleObservation observation = new RuleObservation("payments", AlertType.SERVICE_DOWN, AlertSeverity.CRITICAL,
                "down", "service_status", false, Instant.now());
        when(rules.evaluate()).thenReturn(Map.of("payments", List.of(observation)));
        when(alerts.findByServiceNameAndTypeAndResolvedAtIsNull("payments", AlertType.SERVICE_DOWN))
                .thenReturn(Optional.of(alert));
        when(alerts.save(alert)).thenReturn(alert);
        service.evaluateNow();
        verify(events).save(org.mockito.ArgumentMatchers
                .argThat(event -> event.getEventType() == AlertEvent.EventType.ALERT_RESOLVED));
    }
}
