package com.example.anomaly_service.service;

import com.example.anomaly_service.model.Alert;
import com.example.anomaly_service.model.AlertEvent;
import com.example.anomaly_service.repository.AlertEventRepository;
import com.example.anomaly_service.repository.AlertRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertEvaluationService {
    private final RuleEvaluationService rules;
    private final AlertRepository alerts;
    private final AlertEventRepository events;

    public AlertEvaluationService(RuleEvaluationService rules, AlertRepository alerts, AlertEventRepository events) {
        this.rules = rules;
        this.alerts = alerts;
        this.events = events;
    }

    @Scheduled(fixedDelayString = "${anomaly.scheduler-tick-ms:10000}")
    @Transactional
    public void evaluateScheduled() {
        evaluateNow();
    }

    @Transactional
    public void evaluateNow() {
        for (Map.Entry<String, java.util.List<RuleObservation>> entry : rules.evaluate().entrySet())
            for (RuleObservation observation : entry.getValue())
                reconcile(observation);
    }

    private void reconcile(RuleObservation observation) {
        var open = alerts.findByServiceNameAndTypeAndResolvedAtIsNull(observation.serviceName(), observation.type());
        if (observation.active() && open.isEmpty()) {
            Alert alert = alerts.save(new Alert(observation.serviceName(), observation.type(), observation.severity(),
                    observation.message(), observation.sourceMetric(), observation.observedAt()));
            Alert saved = alert;
            events.save(new AlertEvent(saved.getId(), AlertEvent.EventType.ALERT_CREATED, Instant.now()));
        }
        if (!observation.active() && open.isPresent()) {
            Alert alert = open.get();
            alert.resolve(observation.observedAt());
            alerts.save(alert);
            events.save(new AlertEvent(alert.getId(), AlertEvent.EventType.ALERT_RESOLVED, Instant.now()));
        }
    }
}
