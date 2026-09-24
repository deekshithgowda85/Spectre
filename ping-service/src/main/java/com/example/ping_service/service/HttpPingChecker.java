package com.example.ping_service.service;

import com.example.ping_service.config.PingTargetProperties.Target;
import com.example.ping_service.model.PingCheck;
import com.example.ping_service.model.PingStatus;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class HttpPingChecker {
    private final HttpClient client;
    private final com.example.ping_service.config.PingTargetProperties properties;

    public HttpPingChecker(HttpClient client, com.example.ping_service.config.PingTargetProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public PingCheck check(Target target) {
        Instant started = Instant.now();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(target.getUrl()))
                    .timeout(Duration.ofMillis(properties.getHttpTimeoutMs())).GET().build();
            int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            return new PingCheck(target.getName(), status < 400 ? PingStatus.UP : PingStatus.DOWN, elapsed(started),
                    Instant.now());
        } catch (Exception exception) {
            return new PingCheck(target.getName(), PingStatus.DOWN, elapsed(started), Instant.now());
        }
    }

    private long elapsed(Instant started) {
        return Duration.between(started, Instant.now()).toMillis();
    }
}
