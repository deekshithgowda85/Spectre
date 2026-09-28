package com.example.dashboard_service.controller;

import com.example.dashboard_service.dto.ProjectDtos.CreateRequest;
import com.example.dashboard_service.dto.ProjectDtos.Response;
import com.example.dashboard_service.service.ProjectService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projects;

    public ProjectController(ProjectService projects) {
        this.projects = projects;
    }

    @GetMapping
    List<Response> list(Authentication authentication) {
        return projects.list(authentication.getName());
    }

    @GetMapping("/{id}")
    Response get(@PathVariable UUID id, Authentication authentication) {
        return projects.get(id, authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Response create(@RequestBody CreateRequest request, Authentication authentication) {
        return projects.create(request, authentication.getName());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalid(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage(), "code", "INVALID_PROJECT"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> duplicate(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", exception.getMessage(), "code", "DUPLICATE_PROJECT"));
    }
}
