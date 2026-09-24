package com.example.scraper_service.dto;

import com.example.scraper_service.model.MetricSample;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;

public final class MetricDtos {
    private MetricDtos() {
    }

    public record MetricInput(@NotBlank String metricName, @NotNull BigDecimal value, @NotBlank String unit) {
    }

    public record IngestRequest(@NotBlank String serviceName, Instant timestamp,
            @NotEmpty List<@Valid MetricInput> metrics) {
    }

    public record Response(Long id, String serviceName, String metricName, BigDecimal value, String unit,
            Instant timestamp) {
        public static Response from(MetricSample sample) {
            return new Response(sample.getId(), sample.getServiceName(), sample.getMetricName(), sample.getValue(),
                    sample.getUnit(), sample.getTimestamp());
        }
    }

    public record LatestResponse(String serviceName, List<Response> metrics) {
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        public static <T> PageResponse<T> from(Page<T> result) {
            return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                    result.getTotalElements(), result.getTotalPages());
        }
    }
}
