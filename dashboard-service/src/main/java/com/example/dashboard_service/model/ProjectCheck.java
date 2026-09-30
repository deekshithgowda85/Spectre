package com.example.dashboard_service.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_checks")
public class ProjectCheck {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private UUID projectId;
    @Column(nullable = false, length = 64)
    private String path;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private CheckStatus status;
    private Integer httpStatus;
    private Long responseTimeMs;
    @Column(nullable = false)
    private Instant checkedAt;
    @Column(length = 500)
    private String error;

    protected ProjectCheck() {
    }

    public ProjectCheck(UUID projectId, String path, CheckStatus status, Integer httpStatus,
            Long responseTimeMs, Instant checkedAt, String error) {
        this.projectId = projectId;
        this.path = path;
        this.status = status;
        this.httpStatus = httpStatus;
        this.responseTimeMs = responseTimeMs;
        this.checkedAt = checkedAt;
        this.error = error;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getPath() {
        return path;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public String getError() {
        return error;
    }

    public enum CheckStatus {
        UP, DOWN
    }
}