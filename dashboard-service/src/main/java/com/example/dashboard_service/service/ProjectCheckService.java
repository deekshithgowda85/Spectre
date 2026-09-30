package com.example.dashboard_service.service;

import com.example.dashboard_service.dto.ProjectCheckDtos.Response;
import com.example.dashboard_service.model.Project;
import com.example.dashboard_service.model.ProjectCheck;
import com.example.dashboard_service.repository.ProjectCheckRepository;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectCheckService {
    private static final List<String> PATHS = List.of("/health", "/actuator/health", "/");
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    private final ProjectCheckRepository checks;
    private final HttpClient client;

    @Autowired
    public ProjectCheckService(ProjectCheckRepository checks) {
        this(checks,
                HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NORMAL).build());
    }

    ProjectCheckService(ProjectCheckRepository checks, HttpClient client) {
        this.checks = checks;
        this.client = client;
    }

    public List<Response> list(UUID projectId) {
        return checks.findByProjectIdOrderByCheckedAtDescPathAsc(projectId).stream().map(Response::from).toList();
    }

    public void check(Project project) {
        for (String path : PATHS)
            checks.save(runCheck(project.getId(), project.getUrl(), path));
    }

    private ProjectCheck runCheck(UUID projectId, String baseUrl, String path) {
        Instant checkedAt = Instant.now();
        long started = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(TIMEOUT).GET().build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            long elapsed = Duration.ofNanos(System.nanoTime() - started).toMillis();
            ProjectCheck.CheckStatus status = response.statusCode() < 400 ? ProjectCheck.CheckStatus.UP
                    : ProjectCheck.CheckStatus.DOWN;
            String error = status == ProjectCheck.CheckStatus.DOWN ? "HTTP status " + response.statusCode() : null;
            return new ProjectCheck(projectId, path, status, response.statusCode(), elapsed, checkedAt, error);
        } catch (Exception exception) {
            long elapsed = Duration.ofNanos(System.nanoTime() - started).toMillis();
            String error = exception.getMessage() == null ? exception.getClass().getSimpleName()
                    : exception.getMessage();
            return new ProjectCheck(projectId, path, ProjectCheck.CheckStatus.DOWN, null, elapsed, checkedAt, error);
        }
    }
}