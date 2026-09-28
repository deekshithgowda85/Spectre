package com.example.ping_service.service;

import com.example.ping_service.config.PingTargetProperties;
import com.example.ping_service.config.PingTargetProperties.Target;
import com.example.ping_service.dto.PingDtos.CheckResponse;
import com.example.ping_service.dto.PingDtos.TargetResponse;
import com.example.ping_service.model.PingCheck;
import com.example.ping_service.repository.PingCheckRepository;
import com.example.ping_service.model.DynamicPingTarget;
import com.example.ping_service.repository.DynamicPingTargetRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PingMonitorService {
    private final PingTargetProperties properties;
    private final HttpPingChecker checker;
    private final PingCheckRepository checks;
    private final DynamicPingTargetRepository dynamicTargets;
    private final Map<String, Instant> lastChecks = new ConcurrentHashMap<>();

    public PingMonitorService(PingTargetProperties properties, HttpPingChecker checker, PingCheckRepository checks) {
        this(properties, checker, checks, null);
    }

    @Autowired
    public PingMonitorService(PingTargetProperties properties, HttpPingChecker checker, PingCheckRepository checks,
            DynamicPingTargetRepository dynamicTargets) {
        this.properties = properties;
        this.checker = checker;
        this.checks = checks;
        this.dynamicTargets = dynamicTargets;
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
        if (dynamicTargets != null) {
            for (DynamicPingTarget target : dynamicTargets.findAll()) {
                Instant last = lastChecks.get(target.getTargetName());
                if (last == null || now.toEpochMilli() - last.toEpochMilli() >= 60000) {
                    Target configured = new Target();
                    configured.setName(target.getTargetName());
                    configured.setUrl(target.getUrl());
                    checks.save(checker.check(configured));
                    lastChecks.put(target.getTargetName(), now);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CheckResponse> currentStatuses() {
        return validTargets().stream().map(Target::getName).map(checks::findTopByTargetNameOrderByCheckedAtDesc)
                .flatMap(java.util.Optional::stream).map(CheckResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TargetResponse statusFor(String name, String ownerId) {
        Target target = validTargets().stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null);
        if (target == null && dynamicTargets.findByTargetNameAndOwnerId(name, ownerId).isPresent()) {
            DynamicPingTarget dynamic = dynamicTargets.findByTargetNameAndOwnerId(name, ownerId).orElseThrow();
            target = new Target();
            target.setName(dynamic.getTargetName());
            target.setUrl(dynamic.getUrl());
        }
        if (target == null)
            throw new IllegalArgumentException("Unknown ping target: " + name);
        CheckResponse current = checks.findTopByTargetNameOrderByCheckedAtDesc(target.getName())
                .map(CheckResponse::from).orElse(null);
        List<CheckResponse> history = checks.findTop20ByTargetNameOrderByCheckedAtDesc(target.getName()).stream()
                .map(CheckResponse::from).toList();
        return new TargetResponse(target.getName(), current, history);
    }

    @Transactional(readOnly = true)
    public TargetResponse statusFor(String name) {
        return statusFor(name, "");
    }

    @Transactional
    public void register(String name, String url, String ownerId) {
        if (name == null || name.isBlank() || url == null || url.isBlank())
            throw new IllegalArgumentException("A project target name and URL are required");
        try {
            java.net.URI parsed = java.net.URI.create(url.trim());
            if ((!"http".equalsIgnoreCase(parsed.getScheme()) && !"https".equalsIgnoreCase(parsed.getScheme()))
                    || parsed.getHost() == null)
                throw new IllegalArgumentException("Invalid website URL");
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Please enter a valid website URL.");
        }
        if (dynamicTargets.findByTargetNameAndOwnerId(name, ownerId).isEmpty()
                && dynamicTargets.existsById(name)) {
            throw new IllegalStateException("Target is already registered");
        }
        dynamicTargets.save(new DynamicPingTarget(name, url.trim(), ownerId));
    }

    private List<Target> validTargets() {
        return properties.getTargets().stream()
                .filter(target -> target.getName() != null && !target.getName().isBlank())
                .filter(target -> target.getUrl() != null && !target.getUrl().isBlank()).toList();
    }
}
