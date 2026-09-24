package com.example.incident_service.service;

import com.example.incident_service.config.IncidentProperties;
import java.time.Instant;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AnomalyAlertClient {
    private final RestClient client;
    private final IncidentProperties properties;

    public AnomalyAlertClient(RestClient.Builder builder, IncidentProperties properties) {
        client = builder.baseUrl(properties.getAnomalyUrl()).build();
        this.properties = properties;
    }

    public List<AlertRecord> list(String status) {
        return client.get().uri(uri -> uri.path("/api/anomalies").queryParam("status", status).build())
                .headers(this::auth).retrieve().body(new ParameterizedTypeReference<>() {
                });
    }

    private void auth(HttpHeaders headers) {
        if (properties.getServiceToken() != null && !properties.getServiceToken().isBlank())
            headers.setBearerAuth(properties.getServiceToken());
    }

    public record AlertRecord(Long id, String serviceName, String type, String severity, String message,
            String sourceMetric, Instant triggeredAt, Instant resolvedAt) {
    }
}
