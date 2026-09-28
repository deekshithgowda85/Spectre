package com.example.api_gateway;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    @Bean
    SecurityWebFilterChain gatewaySecurity(
            ServerHttpSecurity http,
            CorsConfigurationSource corsConfigurationSource) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${DASHBOARD_ORIGIN:http://localhost:3000}") String dashboardOrigin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(dashboardOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    CorsWebFilter corsWebFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsWebFilter(corsConfigurationSource);
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    GlobalFilter normalizeCorsResponse(
            @Value("${DASHBOARD_ORIGIN:http://localhost:3000}") String dashboardOrigin) {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            String requestOrigin = exchange.getRequest().getHeaders().getOrigin();
            if (dashboardOrigin.equals(requestOrigin)) {
                exchange.getResponse().getHeaders().set("Access-Control-Allow-Origin", dashboardOrigin);
                exchange.getResponse().getHeaders().set("Access-Control-Allow-Credentials", "true");
            }
        }));
    }
}
