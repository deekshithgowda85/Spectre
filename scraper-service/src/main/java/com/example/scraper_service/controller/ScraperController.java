package com.example.scraper_service.controller;

import com.example.scraper_service.dto.MetricDtos.IngestRequest;
import com.example.scraper_service.dto.MetricDtos.LatestResponse;
import com.example.scraper_service.dto.MetricDtos.Response;
import com.example.scraper_service.dto.MetricDtos.PageResponse;
import com.example.scraper_service.service.MetricIngestionService;
import com.example.scraper_service.service.MetricQueryService;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scraper")
public class ScraperController {
    private final MetricIngestionService ingestion;
    private final MetricQueryService query;

    public ScraperController(MetricIngestionService ingestion, MetricQueryService query) {
        this.ingestion = ingestion;
        this.query = query;
    }

    @PostMapping("/ingest")
    ResponseEntity<java.util.List<Response>> ingest(@Valid @RequestBody IngestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestion.ingest(request));
    }

    @GetMapping("/metrics")
    PageResponse<Response> metrics(@RequestParam(required = false) String service,
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minusSeconds(86400) : from;
        if (start.isAfter(end))
            throw new IllegalArgumentException("from must be before to");
        Pageable pageable = PageRequest.of(Math.min(Math.max(page, 0), 10000), Math.min(Math.max(size, 1), 500),
                Sort.by("timestamp").ascending());
        return PageResponse.from(query.search(service, metric, start, end, pageable));
    }

    @GetMapping("/metrics/latest")
    LatestResponse latest(@RequestParam String service, @RequestParam(defaultValue = "500") int size) {
        return query.latest(service, PageRequest.of(0, Math.min(Math.max(size, 1), 2000)));
    }

    @GetMapping
    java.util.Map<String, String> health() {
        return java.util.Map.of("status", "UP", "service", "scraper-service");
    }
}
