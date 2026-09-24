package com.example.dashboard_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "incidents")
public class IncidentSummaryEntity {
    @Id private Long id;
    @Enumerated(EnumType.STRING) private IncidentStatus status;
    @Enumerated(EnumType.STRING) private IncidentSeverity severity;
    public enum IncidentStatus { OPEN, ACKNOWLEDGED, RESOLVED }
    public enum IncidentSeverity { LOW, MEDIUM, HIGH, CRITICAL }
    public IncidentSummaryEntity() { }
    public IncidentStatus getStatus() { return status; }
    public IncidentSeverity getSeverity() { return severity; }
}
