package com.example.auth_service.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.net.URI;

@Configuration
public class NeonDataSourceConfig {
    @Bean
    DataSource dataSource(Environment environment) {
        String configuredUrl = environment.getProperty("DATABASE_URL");
        String url = configuredUrl == null || configuredUrl.isBlank()
                ? environment.getRequiredProperty("spring.datasource.url")
                : normalize(configuredUrl);
        String username = environment.getProperty("DATABASE_USERNAME",
                environment.getProperty("spring.datasource.username"));
        String password = environment.getProperty("DATABASE_PASSWORD",
                environment.getProperty("spring.datasource.password", ""));
        return DataSourceBuilder.create().url(url).username(username).password(password).build();
    }

    private String normalize(String url) {
        String value = url.startsWith("jdbc:") ? url.substring(5) : url;
        if (!value.contains("://"))
            return "jdbc:postgresql://" + value;
        URI parsed = URI.create(value);
        StringBuilder normalized = new StringBuilder("jdbc:postgresql://").append(parsed.getHost());
        if (parsed.getPort() > 0)
            normalized.append(':').append(parsed.getPort());
        if (parsed.getRawPath() != null)
            normalized.append(parsed.getRawPath());
        if (parsed.getRawQuery() != null)
            normalized.append('?').append(parsed.getRawQuery());
        return normalized.toString();
    }
}
