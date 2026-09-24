package com.example.anomaly_service.repository;

import com.example.anomaly_service.model.AlertEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {
    List<AlertEvent> findByAlertIdOrderByCreatedAtAsc(Long alertId);
}
