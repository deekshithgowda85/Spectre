package com.example.incident_service.model;

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
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "incident_events", indexes = {
        @Index(name = "idx_incident_event_stream", columnList = "incidentId,createdAt"),
        @Index(name = "idx_incident_event_type", columnList = "eventType,createdAt")
})
public class IncidentEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long incidentId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentEventType eventType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false, length = 320)
    private String createdBy;

    protected IncidentEvent() {
    }

    public IncidentEvent(Long incidentId, IncidentEventType eventType, Map<String, Object> payload, Instant createdAt,
            String createdBy) {
        this.incidentId = incidentId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public IncidentEventType getEventType() {
        return eventType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
