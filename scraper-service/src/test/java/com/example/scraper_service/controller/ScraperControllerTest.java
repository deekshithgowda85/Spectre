package com.example.scraper_service.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.scraper_service.dto.MetricDtos.IngestRequest;
import com.example.scraper_service.dto.MetricDtos.LatestResponse;
import com.example.scraper_service.dto.MetricDtos.Response;
import com.example.scraper_service.service.MetricIngestionService;
import com.example.scraper_service.service.MetricQueryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ScraperControllerTest {
    @Mock
    private MetricIngestionService ingestion;
    @Mock
    private MetricQueryService query;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ScraperController(ingestion, query)).build();
    }

    @Test
    void ingestsMetrics() throws Exception {
        Response response = new Response(1L, "payments", "cpu_usage", new BigDecimal("42.5"), "percent", Instant.now());
        when(ingestion.ingest(org.mockito.ArgumentMatchers.any(IngestRequest.class))).thenReturn(List.of(response));
        mvc.perform(post("/api/scraper/ingest").contentType("application/json").content(
                "{\"serviceName\":\"payments\",\"metrics\":[{\"metricName\":\"cpu_usage\",\"value\":42.5,\"unit\":\"percent\"}]}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$[0].metricName", is("cpu_usage")));
    }

    @Test
    void queriesLatestMetrics() throws Exception {
        Response response = new Response(1L, "payments", "cpu_usage", new BigDecimal("42.5"), "percent", Instant.now());
        when(query.latest(org.mockito.ArgumentMatchers.eq("payments"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new LatestResponse("payments", List.of(response)));
        mvc.perform(get("/api/scraper/metrics/latest?service=payments")).andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics[0].value", is(42.5)));
    }

    @Test
    void queriesTimeSeriesPage() throws Exception {
        Response response = new Response(1L, "payments", "cpu_usage", new BigDecimal("42.5"), "percent", Instant.now());
        when(query.search(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(new PageImpl<>(List.of(response)));
        mvc.perform(get("/api/scraper/metrics?service=payments&metric=cpu_usage")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].serviceName", is("payments")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }
}
