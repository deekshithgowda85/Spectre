package com.example.anomaly_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_service_open", columnList = "serviceName,type,resolvedAt"),
        @Index(name = "idx_alert_triggered_at", columnList = "triggeredAt")
})
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String serviceName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(nullable = false, length = 120)
    private String sourceMetric;
    @Column(nullable = false)
    private Instant triggeredAt;
    private Instant resolvedAt;

    protected Alert() {
    }

    public Alert(String serviceName, AlertType type, AlertSeverity severity, String message, String sourceMetric,
            Instant triggeredAt) {
        this.serviceName = serviceName;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.sourceMetric = sourceMetric;
        this.triggeredAt = triggeredAt;
    }

    public void resolve(Instant timestamp) {
        resolvedAt = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public AlertType getType() {
        return type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getSourceMetric() {
        return sourceMetric;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
