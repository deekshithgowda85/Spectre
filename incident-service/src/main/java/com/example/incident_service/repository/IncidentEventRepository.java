package com.example.incident_service.repository;

import com.example.incident_service.model.IncidentEvent;
import com.example.incident_service.model.IncidentEventType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, Long> {
    List<IncidentEvent> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);

    boolean existsByIncidentIdAndEventType(Long incidentId, IncidentEventType eventType);
}
