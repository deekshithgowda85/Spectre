package com.example.anomaly_service.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "anomaly")
public class AnomalyProperties {
    private long schedulerTickMs = 10000;
    private String scraperUrl;
    private String pingUrl;
    private String serviceToken;
    private List<String> monitoredServices = new ArrayList<>();
    private BigDecimal responseTimeThresholdMs = BigDecimal.valueOf(1000);
    private BigDecimal errorRateThreshold = BigDecimal.valueOf(0.05);
    private int consecutiveSamples = 3;

    public long getSchedulerTickMs() {
        return schedulerTickMs;
    }

    public void setSchedulerTickMs(long value) {
        schedulerTickMs = value;
    }

    public String getScraperUrl() {
        return scraperUrl;
    }

    public void setScraperUrl(String value) {
        scraperUrl = value;
    }

    public String getPingUrl() {
        return pingUrl;
    }

    public void setPingUrl(String value) {
        pingUrl = value;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String value) {
        serviceToken = value;
    }

    public List<String> getMonitoredServices() {
        return monitoredServices;
    }

    public void setMonitoredServices(List<String> value) {
        monitoredServices = value;
    }

    public BigDecimal getResponseTimeThresholdMs() {
        return responseTimeThresholdMs;
    }

    public void setResponseTimeThresholdMs(BigDecimal value) {
        responseTimeThresholdMs = value;
    }

    public BigDecimal getErrorRateThreshold() {
        return errorRateThreshold;
    }

    public void setErrorRateThreshold(BigDecimal value) {
        errorRateThreshold = value;
    }

    public int getConsecutiveSamples() {
        return consecutiveSamples;
    }

    public void setConsecutiveSamples(int value) {
        consecutiveSamples = value;
    }
}
