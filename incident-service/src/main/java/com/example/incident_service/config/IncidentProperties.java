package com.example.incident_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "incident")
public class IncidentProperties {
    private long correlationWindowMinutes = 30;
    private long schedulerTickMs = 10000;
    private String anomalyUrl;
    private String serviceToken;

    public long getCorrelationWindowMinutes() {
        return correlationWindowMinutes;
    }

    public void setCorrelationWindowMinutes(long value) {
        correlationWindowMinutes = value;
    }

    public long getSchedulerTickMs() {
        return schedulerTickMs;
    }

    public void setSchedulerTickMs(long value) {
        schedulerTickMs = value;
    }

    public String getAnomalyUrl() {
        return anomalyUrl;
    }

    public void setAnomalyUrl(String value) {
        anomalyUrl = value;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String value) {
        serviceToken = value;
    }
}
