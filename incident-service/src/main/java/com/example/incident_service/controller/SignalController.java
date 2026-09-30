package com.example.incident_service.controller;

import com.example.incident_service.dto.SignalDtos.Response;
import com.example.incident_service.service.SignalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signals")
public class SignalController {
    private final SignalService signals;

    public SignalController(SignalService signals) {
        this.signals = signals;
    }

    @GetMapping
    Response get(@RequestParam(required = false) String service) {
        return signals.get(service);
    }
}
