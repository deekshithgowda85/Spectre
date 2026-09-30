package com.example.dashboard_service.repository;

import com.example.dashboard_service.model.ProjectCheck;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCheckRepository extends JpaRepository<ProjectCheck, UUID> {
    List<ProjectCheck> findByProjectIdOrderByCheckedAtDescPathAsc(UUID projectId);
}