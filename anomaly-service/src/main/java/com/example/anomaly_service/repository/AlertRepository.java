package com.example.anomaly_service.repository;

import com.example.anomaly_service.model.Alert;
import com.example.anomaly_service.model.AlertSeverity;
import com.example.anomaly_service.model.AlertType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Optional<Alert> findByServiceNameAndTypeAndResolvedAtIsNull(String serviceName, AlertType type);

    @Query("select alert from Alert alert where (:service is null or alert.serviceName = :service) and (:severity is null or alert.severity = :severity) and ((:status = 'OPEN' and alert.resolvedAt is null) or (:status = 'RESOLVED' and alert.resolvedAt is not null) or :status is null)")
    List<Alert> search(@Param("service") String service, @Param("severity") AlertSeverity severity,
            @Param("status") String status, Sort sort);
}
