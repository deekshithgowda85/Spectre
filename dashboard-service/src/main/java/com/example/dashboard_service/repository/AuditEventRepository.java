package com.example.dashboard_service.repository;

import com.example.dashboard_service.model.AuditEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findAllByOrderByOccurredAtDesc();
}