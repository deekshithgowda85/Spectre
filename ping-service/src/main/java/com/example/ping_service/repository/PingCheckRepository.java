package com.example.ping_service.repository;

import com.example.ping_service.model.PingCheck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PingCheckRepository extends JpaRepository<PingCheck, Long> {
    Optional<PingCheck> findTopByTargetNameOrderByCheckedAtDesc(String targetName);

    List<PingCheck> findTop20ByTargetNameOrderByCheckedAtDesc(String targetName);

    List<PingCheck> findTop20ByOrderByCheckedAtDesc();
}
