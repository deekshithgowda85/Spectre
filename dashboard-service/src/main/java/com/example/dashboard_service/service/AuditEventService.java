package com.example.dashboard_service.service;

import com.example.dashboard_service.dto.AuditEventDtos.Response;
import com.example.dashboard_service.model.AuditEvent;
import com.example.dashboard_service.repository.AuditEventRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditEventService {
    private final AuditEventRepository events;

    public AuditEventService(AuditEventRepository events) {
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<Response> list() {
        return events.findAllByOrderByOccurredAtDesc().stream().map(Response::from).toList();
    }

    @Transactional
    public void recordProjectCreated(String actor, String resource) {
        events.save(new AuditEvent(actor, AuditEvent.Action.PROJECT_CREATED, resource, resolveIpAddress()));
    }

    private String resolveIpAddress() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return "local";
        }
        String address = servletAttributes.getRequest().getRemoteAddr();
        return address == null || address.isBlank() ? "local" : address;
    }
}