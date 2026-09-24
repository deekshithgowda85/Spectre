package com.example.incident_service.repository;

import com.example.incident_service.model.Incident;
import com.example.incident_service.model.IncidentSeverity;
import com.example.incident_service.model.IncidentStatus;
import java.util.List;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findAllByOrderByOpenedAtDesc();

    List<Incident> findBySeverityAndStatusOrderByOpenedAtDesc(IncidentSeverity severity, IncidentStatus status);

    java.util.Optional<Incident> findFirstByServiceNameAndStatusAndOpenedAtAfterOrderByOpenedAtDesc(String serviceName,
            IncidentStatus status, Instant openedAfter);

    List<Incident> findByStatus(IncidentStatus status);
}
