package com.floodrescue.shared.config;

import com.floodrescue.shared.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC_URLS = {
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/refresh",
        "/actuator/health",
        "/ws/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notifications/sse").authenticated()

                // Auth
                .requestMatchers("/api/auth/**").authenticated()

                // Requests
                .requestMatchers(HttpMethod.POST, "/api/requests").hasRole("CITIZEN")
                .requestMatchers("/api/requests/my").hasRole("CITIZEN")
                .requestMatchers("/api/requests/*/confirm").hasRole("CITIZEN")
                .requestMatchers("/api/requests/*/verify").hasRole("COORDINATOR")
                .requestMatchers("/api/requests/*/cancel").hasAnyRole("CITIZEN", "COORDINATOR")
                .requestMatchers(HttpMethod.GET, "/api/requests/**").hasAnyRole("COORDINATOR", "MANAGER", "ADMIN")

                // Dispatch
                .requestMatchers("/api/dispatch/assign").hasRole("COORDINATOR")
                .requestMatchers("/api/dispatch/assignments/my").hasRole("RESCUE_TEAM")
                .requestMatchers("/api/dispatch/assignments/*/start").hasRole("RESCUE_TEAM")
                .requestMatchers("/api/dispatch/assignments/*/complete").hasRole("RESCUE_TEAM")
                .requestMatchers("/api/dispatch/location").hasRole("RESCUE_TEAM")
                .requestMatchers(HttpMethod.GET, "/api/dispatch/**").hasAnyRole("COORDINATOR", "MANAGER", "ADMIN")

                // Resources
                .requestMatchers("/api/resources/distributions").hasAnyRole("COORDINATOR", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/resources/**").hasAnyRole("COORDINATOR", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/resources/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/resources/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/resources/**").hasAnyRole("MANAGER", "ADMIN")

                // Reports
                .requestMatchers("/api/reports/**").hasAnyRole("MANAGER", "ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitFilter, JwtAuthFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}