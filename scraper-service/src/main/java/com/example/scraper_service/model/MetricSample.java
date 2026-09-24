package com.example.scraper_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "metric_samples", indexes = {
        @Index(name = "idx_metric_service_timestamp", columnList = "serviceName,timestamp"),
        @Index(name = "idx_metric_service_metric_timestamp", columnList = "serviceName,metricName,timestamp")
})
public class MetricSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String serviceName;
    @Column(nullable = false, length = 120)
    private String metricName;
    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal value;
    @Column(nullable = false, length = 40)
    private String unit;
    @Column(nullable = false)
    private Instant timestamp;

    protected MetricSample() {
    }

    public MetricSample(String serviceName, String metricName, BigDecimal value, String unit, Instant timestamp) {
        this.serviceName = serviceName;
        this.metricName = metricName;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMetricName() {
        return metricName;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
