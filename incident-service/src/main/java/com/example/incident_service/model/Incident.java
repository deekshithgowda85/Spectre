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

@Entity
@Table(name = "incidents", indexes = @Index(name = "idx_incident_service_status_opened", columnList = "serviceName,status,openedAt"))
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, length = 5000)
    private String description;
    @Column(nullable = false, length = 120)
    private String serviceName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentSeverity severity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status;
    @Column(nullable = false)
    private Instant openedAt;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    private Instant acknowledgedAt;
    private Instant resolvedAt;
    private Long mtta;
    private Long mttr;
    @Column(nullable = false, length = 320)
    private String createdBy;

    protected Incident() {
    }

    public Incident(String title, String description, String serviceName, IncidentSeverity severity, String createdBy) {
        this.title = title;
        this.description = description;
        this.serviceName = serviceName;
        this.severity = severity;
        this.status = IncidentStatus.OPEN;
        this.openedAt = Instant.now();
        this.createdAt = this.openedAt;
        this.updatedAt = this.openedAt;
        this.createdBy = createdBy;
    }

    public void apply(IncidentProjection projection) {
        title = projection.title();
        description = projection.description();
        serviceName = projection.serviceName();
        severity = projection.severity();
        status = projection.status();
        openedAt = projection.openedAt();
        createdAt = projection.openedAt();
        acknowledgedAt = projection.acknowledgedAt();
        resolvedAt = projection.resolvedAt();
        mtta = projection.mtta();
        mttr = projection.mttr();
        updatedAt = Instant.now();
        createdBy = projection.createdBy();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getServiceName() {
        return serviceName;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Long getMtta() {
        return mtta;
    }

    public Long getMttr() {
        return mttr;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
