package com.example.incident_service.controller;

import com.example.incident_service.dto.IncidentDtos.CreateRequest;
import com.example.incident_service.dto.IncidentDtos.Response;
import com.example.incident_service.dto.IncidentDtos.EventResponse;
import com.example.incident_service.model.IncidentSeverity;
import com.example.incident_service.model.IncidentStatus;
import com.example.incident_service.service.IncidentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    List<Response> list(@RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) IncidentStatus status) {
        return incidentService.list(severity, status);
    }

    @GetMapping("/{id}")
    Response get(@PathVariable Long id) {
        return incidentService.get(id);
    }

    @GetMapping("/{id}/events")
    List<EventResponse> events(@PathVariable Long id) {
        return incidentService.eventStream(id).stream().map(EventResponse::from).toList();
    }

    @PostMapping
    ResponseEntity<Response> create(@Valid @RequestBody CreateRequest request, Principal principal) {
        Response response = incidentService.create(request, principal.getName());
        return ResponseEntity.created(URI.create("/api/incidents/" + response.id())).body(response);
    }

    @PatchMapping("/{id}/acknowledge")
    Response acknowledge(@PathVariable Long id, Principal principal) {
        return incidentService.acknowledge(id, principal.getName());
    }

    @PatchMapping("/{id}/resolve")
    Response resolve(@PathVariable Long id, Principal principal) {
        return incidentService.resolve(id, principal.getName());
    }

    @PatchMapping("/{id}/reopen")
    Response reopen(@PathVariable Long id, Principal principal) {
        return incidentService.reopen(id, principal.getName());
    }

    @GetMapping("/metrics/mtta")
    Map<String, Object> mtta(@RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String service) {
        return metrics("mtta", from, to, service);
    }

    @GetMapping("/metrics/mttr")
    Map<String, Object> mttr(@RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String service) {
        return metrics("mttr", from, to, service);
    }

    private Map<String, Object> metrics(String metric, Instant from, Instant to, String service) {
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minusSeconds(604800) : from;
        if (start.isAfter(end))
            throw new IllegalArgumentException("from must be before to");
        return Map.of("metric", metric, "averageMinutes", incidentService.average(metric, start, end, service), "from",
                start, "to", end);
    }
}
