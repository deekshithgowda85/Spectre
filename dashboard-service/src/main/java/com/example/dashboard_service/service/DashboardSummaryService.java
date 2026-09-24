package com.example.dashboard_service.service;

import com.example.dashboard_service.model.IncidentSummaryEntity.IncidentSeverity;
import com.example.dashboard_service.model.IncidentSummaryEntity.IncidentStatus;
import com.example.dashboard_service.repository.IncidentSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DashboardSummaryService {
    private final IncidentSummaryRepository incidents;
    private final RestClient pingClient;
    public DashboardSummaryService(IncidentSummaryRepository incidents, RestClient.Builder restClientBuilder,
                                   @org.springframework.beans.factory.annotation.Value("${PING_SERVICE_URL:http://localhost:8084}") String pingUrl) {
        this.incidents = incidents;
        this.pingClient = restClientBuilder.baseUrl(pingUrl).build();
    }
    public Summary getSummary() {
        Long servicesUp = pingIsUp() ? 1L : 0L;
        return new Summary(incidents.count(), incidents.countByStatus(IncidentStatus.OPEN),
            incidents.countByStatus(IncidentStatus.ACKNOWLEDGED), incidents.countByStatus(IncidentStatus.RESOLVED),
            incidents.countBySeverity(IncidentSeverity.CRITICAL), null, servicesUp, 1L - servicesUp, null, null);
    }
    private boolean pingIsUp() { try { pingClient.get().uri("/api/ping").retrieve().toBodilessEntity(); return true; } catch (RuntimeException exception) { return false; } }
    public record Summary(long totalIncidents, long openIncidents, long acknowledgedIncidents,
                          long resolvedIncidents, long criticalIncidents, Long activeAlerts,
                          Long servicesUp, Long servicesDown, Long avgMtta, Long avgMttr) { }
}
