package com.example.ping_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ping_checks", indexes = @Index(name = "idx_ping_target_checked_at", columnList = "targetName,checkedAt"))
public class PingCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String targetName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PingStatus status;
    @Column(nullable = false)
    private long latencyMs;
    @Column(nullable = false)
    private Instant checkedAt;

    protected PingCheck() {
    }

    public PingCheck(String targetName, PingStatus status, long latencyMs, Instant checkedAt) {
        this.targetName = targetName;
        this.status = status;
        this.latencyMs = latencyMs;
        this.checkedAt = checkedAt;
    }

    public String getTargetName() {
        return targetName;
    }

    public PingStatus getStatus() {
        return status;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }
}
