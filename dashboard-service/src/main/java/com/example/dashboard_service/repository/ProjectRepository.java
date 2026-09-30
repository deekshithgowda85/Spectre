package com.example.dashboard_service.repository;

import com.example.dashboard_service.model.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByOwnerIdOrderByCreatedAtDesc(String ownerId);

    List<Project> findByStatus(Project.ProjectStatus status);

    Optional<Project> findByIdAndOwnerId(UUID id, String ownerId);

    boolean existsByOwnerIdAndUrl(String ownerId, String url);
}
