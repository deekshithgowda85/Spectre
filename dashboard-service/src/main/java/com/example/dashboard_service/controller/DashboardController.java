package com.example.dashboard_service.controller;

import com.example.dashboard_service.service.DashboardSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardSummaryService summaryService;
    public DashboardController(DashboardSummaryService summaryService) { this.summaryService = summaryService; }
    @GetMapping("/summary")
    DashboardSummaryService.Summary summary() { return summaryService.getSummary(); }
}
