package com.example.ping_service.controller;

import com.example.ping_service.dto.PingDtos.CheckResponse;
import com.example.ping_service.dto.PingDtos.TargetResponse;
import com.example.ping_service.service.PingMonitorService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
public class PingController {
    private final PingMonitorService monitor;

    public PingController(PingMonitorService monitor) {
        this.monitor = monitor;
    }

    @GetMapping("/status")
    List<CheckResponse> status() {
        return monitor.currentStatuses();
    }

    @GetMapping("/status/{name}")
    TargetResponse status(@PathVariable String name) {
        return monitor.statusFor(name);
    }

    @GetMapping
    Map<String, String> health() {
        return Map.of("status", "UP", "service", "ping-service", "timestamp", Instant.now().toString());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> notFound(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not_found", "code", "TARGET_NOT_FOUND", "detail", exception.getMessage()));
    }
}
