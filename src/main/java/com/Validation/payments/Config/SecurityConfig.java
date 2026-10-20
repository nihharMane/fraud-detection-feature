
package com.Validation.payments.Config;

import com.Validation.payments.Filters.HmacFilter;
import com.Validation.payments.util.HmacSHA256Util;
import com.Validation.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
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



@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final HmacSHA256Util hmacSHA256Util;
    private final JsonUtil jsonUtil;

    // 1. Merchant payment requests: preserve HMAC authentication
    @Bean
    @Order(1)
    public SecurityFilterChain paymentsFilterChain(
            HttpSecurity http) throws Exception {

        http
                .securityMatcher("/v1/payments/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .addFilterAfter(
                        new HmacFilter(hmacSHA256Util, jsonUtil),
                        LogoutFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/payments/health").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    // 2. Login endpoint: no JWT required to log in


    @Bean
    @Order(2)
    public SecurityFilterChain authFilterChain(
            HttpSecurity http) throws Exception {

        http
                .securityMatcher("/v1/auth/**")
                .cors(cors ->
                        cors.configurationSource(authCorsSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/auth/register",
                                "/v1/auth/login",
                                "/v1/auth/refresh",
                                "/v1/auth/logout"
                        ).permitAll()
                        .anyRequest().denyAll()
                );

        return http.build();
    }

    private CorsConfigurationSource authCorsSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://fraud-shield-dashboard.onrender.com"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Content-Type", "Authorization"
        ));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/v1/auth/**", config);

        return source;
    }



    // 3. Fraud dashboard: valid JWT required

    @Bean
    @Order(3)
    public SecurityFilterChain fraudDashboardFilterChain(
            HttpSecurity http) throws Exception {

        http
                .securityMatcher("/v1/fraud/**")
                .csrf(csrf -> csrf.disable())
                .cors(cors ->
                        cors.configurationSource(fraudDashboardCorsSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Admin-only action
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/v1/fraud/*/override"
                        ).hasRole("ADMIN")

                        // Both roles can read dashboard data
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/v1/fraud/**"
                        ).hasAnyRole("ADMIN", "USER")

                        // Deny other operations by default
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }


    private Converter<Jwt, AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        return jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");

            var authorities = roles == null
                    ? List.<SimpleGrantedAuthority>of()
                    : roles.stream()
                    .map(role -> role.startsWith("ROLE_")
                            ? role
                            : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return new JwtAuthenticationToken(
                    jwt, authorities, jwt.getSubject());
        };
    }



    @Bean
    @Order(4)
    public SecurityFilterChain fallbackFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().denyAll());

        return http.build();
    }


    private CorsConfigurationSource fraudDashboardCorsSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://fraud-shield-dashboard.onrender.com"
        ));

        config.setAllowedMethods(List.of(
                "GET", "PUT", "POST", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/v1/fraud/**", config);

        return source;
    }
}
