package com.floodrescue.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Global Gateway filter that:
 * 1. ALWAYS strips client-supplied X-User-Id / X-User-Role headers (prevent forge)
 * 2. Skips JWT validation for public endpoints
 * 3. Validates JWT for all other requests
 * 4. Forwards X-User-Id and X-User-Role headers to downstream services
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USER_ID   = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    // Nested secret to match app.jwt.secret in YAML
    private JwtProperties jwt = new JwtProperties();
    
    private List<String> publicPaths = new ArrayList<>();

    @Getter
    @Setter
    public static class JwtProperties {
        private String secret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Always strip client-supplied identity headers first to prevent forge
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_USER_ROLE);
                });

        // Skip JWT validation for public endpoints (identity headers already stripped)
        if (isPublicPath(path)) {
            return chain.filter(
                    exchange.mutate().request(requestBuilder.build()).build());
        }

        // WebSocket paths use token query param, handled by backend WebSocketAuthInterceptor
        if (path.startsWith("/ws")) {
            return chain.filter(
                    exchange.mutate().request(requestBuilder.build()).build());
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            String secret = jwt.getSecret();
            if (secret == null) {
                log.error("Gateway: JWT secret is NOT configured!");
                return unauthorized(exchange);
            }

            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String role   = claims.get("role", String.class);

            // Add validated identity headers (client-supplied ones already stripped above)
            ServerHttpRequest mutatedRequest = requestBuilder
                    .header(HEADER_USER_ID, userId)
                    .header(HEADER_USER_ROLE, role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Gateway: JWT expired for path {}", path);
            return unauthorized(exchange);
        } catch (JwtException e) {
            log.warn("Gateway: Invalid JWT for path {}: {}", path, e.getMessage());
            return unauthorized(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }

    private final org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
