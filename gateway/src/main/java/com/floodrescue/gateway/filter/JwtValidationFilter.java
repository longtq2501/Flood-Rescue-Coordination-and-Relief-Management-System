package com.floodrescue.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
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
@Component
@ConfigurationProperties(prefix = "app")
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    private static final String HEADER_USER_ID   = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    // Nested secret to match app.jwt.secret in YAML
    private JwtProperties jwt = new JwtProperties();
    
    private List<String> publicPaths = new ArrayList<>();

    public JwtProperties getJwt() {
        return jwt;
    }

    public void setJwt(JwtProperties jwt) {
        this.jwt = jwt;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public static class JwtProperties {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }



    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info("Gateway: Processing request for path: {}", path);

        // Always strip client-supplied identity headers first to prevent forge
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_USER_ROLE);
                });

        // CRITICAL BYPASS: Always allow Images and public paths to pass through Gateway validation
        if (path.contains("/images") || isPublicPath(path)) {
            log.info("Gateway: Bypassing validation for path: {}", path);
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }

        // WebSocket paths
        if (path.startsWith("/ws")) {
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }

        // Skip JWT validation for OPTIONS requests (CORS preflight)
        if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }

        try {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                token = exchange.getRequest().getQueryParams().getFirst("token");
            }

            if (token == null) {
                log.warn("Gateway: Missing token for path: {}", path);
                return unauthorized(exchange);
            }

            String secret = jwt.getSecret();
            if (secret == null || secret.isEmpty()) {
                log.error("Gateway: JWT secret is NOT configured! Falling back to unauthorized.");
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
            
            log.debug("Gateway: Validated JWT for user {}, role {}, path {}", userId, role, path);

            // Add validated identity headers
            ServerHttpRequest mutatedRequest = requestBuilder
                    .header(HEADER_USER_ID, userId)
                    .header(HEADER_USER_ROLE, role != null ? role : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Gateway: JWT expired for path {}: {}", path, e.getMessage());
            return unauthorized(exchange);
        } catch (Exception e) {
            log.error("Gateway: Validation error for path {}: {}", path, e.getMessage());
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
