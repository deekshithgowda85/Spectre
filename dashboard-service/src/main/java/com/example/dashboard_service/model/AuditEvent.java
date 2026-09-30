package com.example.dashboard_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private Action action;

    @Column(nullable = false, length = 160)
    private String resource;

    @Column(nullable = false, length = 64)
    private String ipAddress;

    @Column(nullable = false)
    private Instant occurredAt;

    protected AuditEvent() {
    }

    public AuditEvent(String actor, Action action, String resource, String ipAddress) {
        this.actor = actor;
        this.action = action;
        this.resource = resource;
        this.ipAddress = ipAddress;
        this.occurredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getActor() {
        return actor;
    }

    public Action getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public enum Action {
        PROJECT_CREATED
    }
}