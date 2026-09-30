package com.example.dashboard_service.controller;

import com.example.dashboard_service.dto.AuditEventDtos.Response;
import com.example.dashboard_service.service.AuditEventService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/audit")
public class AuditEventController {
    private final AuditEventService auditEvents;

    public AuditEventController(AuditEventService auditEvents) {
        this.auditEvents = auditEvents;
    }

    @GetMapping
    List<Response> list() {
        return auditEvents.list();
    }
}