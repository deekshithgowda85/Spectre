package com.example.ping_service.repository;

import com.example.ping_service.model.DynamicPingTarget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DynamicPingTargetRepository extends JpaRepository<DynamicPingTarget, String> {
    List<DynamicPingTarget> findByOwnerId(String ownerId);

    Optional<DynamicPingTarget> findByTargetNameAndOwnerId(String targetName, String ownerId);
}
