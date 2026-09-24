package com.example.scraper_service.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

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
        int start = value.indexOf("://") + 3;
        int end = value.indexOf('/', start);
        int credentials = value.lastIndexOf('@', end);
        return "jdbc:postgresql://" + value.substring(credentials + 1, end) + value.substring(end);
    }
}
