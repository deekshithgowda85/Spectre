package com.example.dashboard_service.dto;

import com.example.dashboard_service.model.Project;
import java.time.Instant;
import java.util.UUID;

public final class ProjectDtos {
    private ProjectDtos() {
    }

    public record CreateRequest(String name, String url) {
    }

    public record Response(UUID id, String name, String url, String status,
            Instant createdAt, Instant updatedAt) {
        public static Response from(Project project) {
            return new Response(project.getId(), project.getName(), project.getUrl(),
                    project.getStatus().name(), project.getCreatedAt(), project.getUpdatedAt());
        }
    }
}
