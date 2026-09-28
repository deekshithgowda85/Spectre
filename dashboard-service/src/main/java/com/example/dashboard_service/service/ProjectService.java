package com.example.dashboard_service.service;

import com.example.dashboard_service.dto.ProjectDtos.CreateRequest;
import com.example.dashboard_service.dto.ProjectDtos.Response;
import com.example.dashboard_service.model.Project;
import com.example.dashboard_service.repository.ProjectRepository;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    @Transactional(readOnly = true)
    public List<Response> list(String ownerId) {
        return projects.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(Response::from).toList();
    }

    @Transactional(readOnly = true)
    public Response get(UUID id, String ownerId) {
        return Response.from(require(id, ownerId));
    }

    @Transactional
    public Response create(CreateRequest request, String ownerId) {
        validate(request);
        String url = normalizeUrl(request.url());
        if (projects.existsByOwnerIdAndUrl(ownerId, url)) {
            throw new IllegalStateException("This website is already being monitored.");
        }
        return Response.from(projects.save(new Project(request.name().trim(), url, ownerId)));
    }

    private Project require(UUID id, String ownerId) {
        return projects.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    private String normalizeUrl(String value) {
        String url = value.trim();
        URI parsed;
        try {
            parsed = URI.create(url);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Please enter a valid website URL.");
        }
        if (!"http".equalsIgnoreCase(parsed.getScheme()) && !"https".equalsIgnoreCase(parsed.getScheme())
                || parsed.getHost() == null) {
            throw new IllegalArgumentException("Please enter a valid website URL.");
        }
        return parsed.toString();
    }

    private void validate(CreateRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()
                || request.name().trim().length() > 120 || request.url() == null
                || request.url().isBlank() || request.url().trim().length() > 2048) {
            throw new IllegalArgumentException("Project name and a valid website URL are required.");
        }
    }
}
