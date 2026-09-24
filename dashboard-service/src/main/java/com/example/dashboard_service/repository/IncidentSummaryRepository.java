package com.example.dashboard_service.repository;

import com.example.dashboard_service.model.IncidentSummaryEntity;
import com.example.dashboard_service.model.IncidentSummaryEntity.IncidentSeverity;
import com.example.dashboard_service.model.IncidentSummaryEntity.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentSummaryRepository extends JpaRepository<IncidentSummaryEntity, Long> {
    long countByStatus(IncidentStatus status);
    long countBySeverity(IncidentSeverity severity);
}
