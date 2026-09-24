package com.example.scraper_service.config;

import java.net.http.HttpClient;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ScraperTargetProperties.class)
public class ScraperConfiguration {
    @Bean
    HttpClient httpClient(ScraperTargetProperties properties) {
        return HttpClient.newBuilder().connectTimeout(Duration.ofMillis(properties.getHttpTimeoutMs())).build();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
