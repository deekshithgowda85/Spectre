package com.example.ping_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ping_service.config.PingTargetProperties;
import com.example.ping_service.config.PingTargetProperties.Target;
import com.example.ping_service.model.PingCheck;
import com.example.ping_service.model.PingStatus;
import com.example.ping_service.repository.PingCheckRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PingMonitorServiceTest {
    @Mock
    private HttpPingChecker checker;
    @Mock
    private PingCheckRepository checks;
    @InjectMocks
    private PingMonitorService monitor;

    @Test
    void scheduledCheckPersistsCheckerResult() {
        PingTargetProperties properties = new PingTargetProperties();
        Target target = new Target();
        target.setName("gateway");
        target.setUrl("http://localhost:8080");
        target.setIntervalMs(1000);
        properties.setTargets(List.of(target));
        PingCheck result = new PingCheck("gateway", PingStatus.UP, 12, Instant.now());
        when(checker.check(target)).thenReturn(result);
        monitor = new PingMonitorService(properties, checker, checks);

        monitor.runScheduledChecks();

        verify(checker).check(target);
        verify(checks).save(result);
    }
}
