package com.example.ping_service.service;

import com.example.ping_service.config.PingTargetProperties;
import com.example.ping_service.config.PingTargetProperties.Target;
import com.example.ping_service.dto.PingDtos.CheckResponse;
import com.example.ping_service.dto.PingDtos.TargetResponse;
import com.example.ping_service.model.PingCheck;
import com.example.ping_service.repository.PingCheckRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PingMonitorService {
    private final PingTargetProperties properties;
    private final HttpPingChecker checker;
    private final PingCheckRepository checks;
    private final Map<String, Instant> lastChecks = new ConcurrentHashMap<>();

    public PingMonitorService(PingTargetProperties properties, HttpPingChecker checker, PingCheckRepository checks) {
        this.properties = properties;
        this.checker = checker;
        this.checks = checks;
    }

    @Scheduled(fixedDelayString = "${ping.scheduler-tick-ms:1000}")
    @Transactional
    public void runScheduledChecks() {
        Instant now = Instant.now();
        for (Target target : validTargets()) {
            Instant last = lastChecks.get(target.getName());
            if (last == null || now.toEpochMilli() - last.toEpochMilli() >= target.getIntervalMs()) {
                checks.save(checker.check(target));
                lastChecks.put(target.getName(), now);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CheckResponse> currentStatuses() {
        return validTargets().stream().map(Target::getName).map(checks::findTopByTargetNameOrderByCheckedAtDesc)
                .flatMap(java.util.Optional::stream).map(CheckResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TargetResponse statusFor(String name) {
        Target target = validTargets().stream().filter(item -> item.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ping target: " + name));
        CheckResponse current = checks.findTopByTargetNameOrderByCheckedAtDesc(target.getName())
                .map(CheckResponse::from).orElse(null);
        List<CheckResponse> history = checks.findTop20ByTargetNameOrderByCheckedAtDesc(target.getName()).stream()
                .map(CheckResponse::from).toList();
        return new TargetResponse(target.getName(), current, history);
    }

    private List<Target> validTargets() {
        return properties.getTargets().stream()
                .filter(target -> target.getName() != null && !target.getName().isBlank())
                .filter(target -> target.getUrl() != null && !target.getUrl().isBlank()).toList();
    }
}
