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
@Table(name = "alert_events", indexes = @Index(name = "idx_alert_event_created_at", columnList = "createdAt"))
public class AlertEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long alertId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventType eventType;
    @Column(nullable = false)
    private Instant createdAt;

    protected AlertEvent() {
    }

    public AlertEvent(Long alertId, EventType eventType, Instant createdAt) {
        this.alertId = alertId;
        this.eventType = eventType;
        this.createdAt = createdAt;
    }

    public enum EventType {
        ALERT_CREATED, ALERT_RESOLVED
    }

    public Long getAlertId() {
        return alertId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
