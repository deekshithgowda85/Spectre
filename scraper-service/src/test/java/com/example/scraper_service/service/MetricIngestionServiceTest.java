package com.example.scraper_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.scraper_service.dto.MetricDtos.IngestRequest;
import com.example.scraper_service.dto.MetricDtos.MetricInput;
import com.example.scraper_service.model.MetricSample;
import com.example.scraper_service.repository.MetricSampleRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricIngestionServiceTest {
    @Mock
    private MetricSampleRepository samples;
    @InjectMocks
    private MetricIngestionService ingestion;

    @Test
    void storesPushedMetrics() {
        when(samples.save(any(MetricSample.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = ingestion.ingest(new IngestRequest("payments", null,
                List.of(new MetricInput("error_rate", new BigDecimal("0.12"), "ratio"))));
        assertEquals(1, result.size());
        assertEquals("payments", result.getFirst().serviceName());
        assertEquals("error_rate", result.getFirst().metricName());
    }
}
