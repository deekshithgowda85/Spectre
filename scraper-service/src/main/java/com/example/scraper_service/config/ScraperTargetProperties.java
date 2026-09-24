package com.example.scraper_service.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scraper")
public class ScraperTargetProperties {
    private long schedulerTickMs = 1000;
    private long httpTimeoutMs = 5000;
    private List<Target> targets = new ArrayList<>();

    public long getSchedulerTickMs() {
        return schedulerTickMs;
    }

    public void setSchedulerTickMs(long value) {
        schedulerTickMs = value;
    }

    public long getHttpTimeoutMs() {
        return httpTimeoutMs;
    }

    public void setHttpTimeoutMs(long value) {
        httpTimeoutMs = value;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> value) {
        targets = value;
    }

    public static class Target {
        private String serviceName;
        private String metricsUrl;
        private long intervalMs = 60000;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String value) {
            serviceName = value;
        }

        public String getMetricsUrl() {
            return metricsUrl;
        }

        public void setMetricsUrl(String value) {
            metricsUrl = value;
        }

        public long getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(long value) {
            intervalMs = value;
        }
    }
}
