package com.example.ping_service.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ping")
public class PingTargetProperties {
    private long schedulerTickMs = 1000;
    private long httpTimeoutMs = 5000;
    private List<Target> targets = new ArrayList<>();

    public long getSchedulerTickMs() {
        return schedulerTickMs;
    }

    public void setSchedulerTickMs(long schedulerTickMs) {
        this.schedulerTickMs = schedulerTickMs;
    }

    public long getHttpTimeoutMs() {
        return httpTimeoutMs;
    }

    public void setHttpTimeoutMs(long httpTimeoutMs) {
        this.httpTimeoutMs = httpTimeoutMs;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    public static class Target {
        private String name;
        private String url;
        private long intervalMs = 60000;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(long intervalMs) {
            this.intervalMs = intervalMs;
        }
    }
}
