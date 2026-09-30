package com.example.incident_service.service;

import com.example.incident_service.dto.SignalDtos.Alert;
import com.example.incident_service.dto.SignalDtos.Metric;
import com.example.incident_service.dto.SignalDtos.Response;
import com.example.incident_service.dto.SignalDtos.ServiceHealth;
import com.example.incident_service.model.Incident;
import com.example.incident_service.model.IncidentStatus;
import com.example.incident_service.repository.IncidentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignalService {
    private final IncidentRepository incidents;

    public SignalService(IncidentRepository incidents) {
        this.incidents = incidents;
    }

    @Transactional(readOnly = true)
    public Response get(String service) {
        List<Incident> records = incidents.findAllByOrderByOpenedAtDesc().stream()
                .filter(item -> service == null || service.isBlank() || service.equals(item.getServiceName()))
                .toList();
        return new Response(alerts(records), services(records), metrics(records));
    }

    private List<Alert> alerts(List<Incident> records) {
        return records.stream().map(item -> new Alert("INC-" + item.getId(), item.getCreatedBy(), item.getServiceName(),
                item.getSeverity().name(), item.getTitle(), item.getOpenedAt(), String.valueOf(item.getId()))).toList();
    }

    private List<ServiceHealth> services(List<Incident> records) {
        Map<String, List<Incident>> grouped = records.stream().collect(Collectors.groupingBy(Incident::getServiceName));
        return grouped.entrySet().stream().map(entry -> {
            long open = entry.getValue().stream().filter(item -> item.getStatus() != IncidentStatus.RESOLVED).count();
            return new ServiceHealth(entry.getKey(), open == 0 ? "UP" : "DEGRADED", open, entry.getValue().size());
        }).sorted(java.util.Comparator.comparing(ServiceHealth::name)).toList();
    }

    private List<Metric> metrics(List<Incident> records) {
        Instant measuredAt = Instant.now();
        long open = records.stream().filter(item -> item.getStatus() != IncidentStatus.RESOLVED).count();
        long critical = records.stream().filter(item -> "CRITICAL".equals(item.getSeverity().name())).count();
        return List.of(new Metric("incidents", records.size(), "count", measuredAt),
                new Metric("open-incidents", open, "count", measuredAt),
                new Metric("critical-incidents", critical, "count", measuredAt));
    }
}
