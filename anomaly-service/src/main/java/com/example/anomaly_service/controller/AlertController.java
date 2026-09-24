package com.example.anomaly_service.controller;

import com.example.anomaly_service.dto.AlertDtos.Response;
import com.example.anomaly_service.model.AlertSeverity;
import com.example.anomaly_service.repository.AlertRepository;
import com.example.anomaly_service.service.AlertEvaluationService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/anomalies")
public class AlertController {
    private final AlertRepository alerts;
    private final AlertEvaluationService evaluation;

    public AlertController(AlertRepository alerts, AlertEvaluationService evaluation) {
        this.alerts = alerts;
        this.evaluation = evaluation;
    }

    @GetMapping
    List<Response> list(@RequestParam(required = false) String service,
            @RequestParam(required = false) AlertSeverity severity, @RequestParam(required = false) String status) {
        return alerts.search(service, severity, status == null ? null : status.toUpperCase(),
                Sort.by(Sort.Direction.DESC, "triggeredAt")).stream().map(Response::from).toList();
    }

    @GetMapping("/{id}")
    Response get(@PathVariable Long id) {
        return alerts.findById(id).map(Response::from)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
    }

    @org.springframework.web.bind.annotation.PostMapping("/evaluate")
    java.util.Map<String, String> evaluate() {
        evaluation.evaluateNow();
        return java.util.Map.of("status", "evaluated");
    }
}
