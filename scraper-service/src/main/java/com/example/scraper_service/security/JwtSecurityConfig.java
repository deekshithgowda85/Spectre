package com.example.scraper_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class JwtSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("${JWT_SECRET}") String secret) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/scraper", "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(secret), UsernamePasswordAuthenticationFilter.class).build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("JWT authentication is required");
        };
    }

    private static final class JwtFilter extends OncePerRequestFilter {
        private final SecretKey key;

        private JwtFilter(String secret) {
            if (secret.length() < 32)
                throw new IllegalArgumentException("JWT_SECRET must be at least 32 characters");
            key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, java.io.IOException {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer "))
                try {
                    var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7))
                            .getPayload();
                    String role = claims.get("role", String.class);
                    var authorities = role == null ? List.<SimpleGrantedAuthority>of()
                            : List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities));
                } catch (RuntimeException ignored) {
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                }
            chain.doFilter(request, response);
        }
    }
}
