package com.example.ping_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "dynamic_ping_targets")
public class DynamicPingTarget {
    @Id
    @Column(length = 120)
    private String targetName;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false, length = 160)
    private String ownerId;

    @Column(nullable = false)
    private Instant createdAt;

    protected DynamicPingTarget() {
    }

    public DynamicPingTarget(String targetName, String url, String ownerId) {
        this.targetName = targetName;
        this.url = url;
        this.ownerId = ownerId;
        this.createdAt = Instant.now();
    }

    public String getTargetName() {
        return targetName;
    }

    public String getUrl() {
        return url;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
