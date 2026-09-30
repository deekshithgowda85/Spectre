package com.example.dashboard_service.service;

import com.example.dashboard_service.model.Project;
import com.example.dashboard_service.repository.ProjectRepository;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProjectCheckScheduler {
    private final ProjectRepository projects;
    private final ProjectCheckService checks;

    public ProjectCheckScheduler(ProjectRepository projects, ProjectCheckService checks) {
        this.projects = projects;
        this.checks = checks;
    }

    @Scheduled(fixedDelayString = "${PROJECT_CHECK_INTERVAL_MS:60000}")
    public void checkMonitoringProjects() {
        List<Project> monitoringProjects = projects.findByStatus(Project.ProjectStatus.MONITORING);
        monitoringProjects.forEach(checks::check);
    }
}