package com.example.dashboard_service.dto;

import com.example.dashboard_service.model.ProjectCheck;
import java.time.Instant;
import java.util.UUID;

public final class ProjectCheckDtos {
    private ProjectCheckDtos() {
    }

    public record Response(UUID id, String path, String status, Integer httpStatus,
            Long responseTimeMs, Instant checkedAt, String error) {
        public static Response from(ProjectCheck check) {
            return new Response(check.getId(), check.getPath(), check.getStatus().name(),
                    check.getHttpStatus(), check.getResponseTimeMs(), check.getCheckedAt(), check.getError());
        }
    }
}