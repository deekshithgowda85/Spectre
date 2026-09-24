package com.example.scraper_service.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.scraper_service.config.ScraperTargetProperties;
import com.example.scraper_service.config.ScraperTargetProperties.Target;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PullScraperServiceTest {
    @Mock
    private HttpClient client;
    @Mock
    private HttpResponse<String> response;
    @Mock
    private MetricIngestionService ingestion;

    @Test
    void pullsJsonMetricsIntoSamples() throws Exception {
        ScraperTargetProperties properties = new ScraperTargetProperties();
        Target target = new Target();
        target.setServiceName("payments");
        target.setMetricsUrl("http://metrics");
        target.setIntervalMs(1);
        properties.setTargets(java.util.List.of(target));
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"cpu_usage\":42.5}");
        when(client.send(org.mockito.ArgumentMatchers.any(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(response);
        new PullScraperService(properties, client, new ObjectMapper(), ingestion).pullDueTargets();
        verify(ingestion).save(eq("payments"), eq("cpu_usage"), eq(new java.math.BigDecimal("42.5")), eq("unknown"),
                org.mockito.ArgumentMatchers.any());
    }
}
