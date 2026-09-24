package com.example.ping_service.dto;

import com.example.ping_service.model.PingCheck;
import com.example.ping_service.model.PingStatus;
import java.time.Instant;
import java.util.List;

public final class PingDtos {
    private PingDtos() {
    }

    public record CheckResponse(String targetName, PingStatus status, long latencyMs, Instant checkedAt) {
        public static CheckResponse from(PingCheck check) {
            return new CheckResponse(check.getTargetName(), check.getStatus(), check.getLatencyMs(),
                    check.getCheckedAt());
        }
    }

    public record TargetResponse(String targetName, CheckResponse current, List<CheckResponse> history) {
    }
}
