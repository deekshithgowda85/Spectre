package com.example.anomaly_service.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.anomaly_service.model.Alert;
import com.example.anomaly_service.model.AlertSeverity;
import com.example.anomaly_service.model.AlertType;
import com.example.anomaly_service.repository.AlertRepository;
import com.example.anomaly_service.service.AlertEvaluationService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {
    @Mock
    private AlertRepository alerts;
    @Mock
    private AlertEvaluationService evaluation;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new AlertController(alerts, evaluation)).build();
    }

    @Test
    void listsAlerts() throws Exception {
        Alert alert = new Alert("payments", AlertType.SERVICE_DOWN, AlertSeverity.CRITICAL, "down", "service_status",
                Instant.now());
        when(alerts.search(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any())).thenReturn(List.of(alert));
        mvc.perform(get("/api/anomalies")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName", is("payments")))
                .andExpect(jsonPath("$[0].type", is("SERVICE_DOWN")));
    }
}
