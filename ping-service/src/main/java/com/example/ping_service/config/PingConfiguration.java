package com.example.ping_service.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PingTargetProperties.class)
public class PingConfiguration {
    @Bean
    HttpClient httpClient(PingTargetProperties properties) {
        return HttpClient.newBuilder().connectTimeout(Duration.ofMillis(properties.getHttpTimeoutMs())).build();
    }
}
