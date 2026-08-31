package com.Validation.payments.Config;

import com.Validation.payments.security.Filters.HmacFilter;
import com.Validation.payments.util.HmacSHA256Util;
import com.Validation.payments.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final HmacSHA256Util hmacSHA256Util;
    private final JsonUtil jsonUtil;

    /**
     * Merchant-facing payment submission - stays HMAC-signed, unchanged behavior.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain paymentsFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/v1/payments/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(new HmacFilter(hmacSHA256Util, jsonUtil), LogoutFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Internal fraud-dashboard reads/overrides - no HMAC (no client-signed body on GET),
     * CORS-open to the local React dev server. Swap permitAll for real auth (API key /
     * JWT for an ops role) before this is ever exposed publicly.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain fraudDashboardFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/v1/fraud/**")
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(fraudDashboardCorsSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    private CorsConfigurationSource fraudDashboardCorsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "PUT", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/v1/fraud/**", config);
        return source;
    }
}