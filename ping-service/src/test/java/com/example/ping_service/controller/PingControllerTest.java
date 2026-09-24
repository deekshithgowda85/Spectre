package com.example.ping_service.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ping_service.dto.PingDtos.CheckResponse;
import com.example.ping_service.dto.PingDtos.TargetResponse;
import com.example.ping_service.model.PingStatus;
import com.example.ping_service.service.PingMonitorService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PingControllerTest {
    @Mock
    private PingMonitorService monitor;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new PingController(monitor)).build();
    }

    @Test
    void returnsCurrentStatuses() throws Exception {
        CheckResponse response = new CheckResponse("gateway", PingStatus.UP, 18, Instant.now());
        when(monitor.currentStatuses()).thenReturn(List.of(response));
        mvc.perform(get("/api/ping/status")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetName", is("gateway"))).andExpect(jsonPath("$[0].status", is("UP")));
    }

    @Test
    void returnsTargetHistory() throws Exception {
        CheckResponse response = new CheckResponse("gateway", PingStatus.DOWN, 5000, Instant.now());
        when(monitor.statusFor("gateway")).thenReturn(new TargetResponse("gateway", response, List.of(response)));
        mvc.perform(get("/api/ping/status/gateway")).andExpect(status().isOk())
                .andExpect(jsonPath("$.current.status", is("DOWN")))
                .andExpect(jsonPath("$.history[0].latencyMs", is(5000)));
    }
}
