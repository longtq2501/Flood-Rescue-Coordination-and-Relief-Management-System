package com.floodrescue.report.shared.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT utility for the backend monolith.
 * Only validates tokens and extracts claims — does NOT generate tokens
 * (token generation is done exclusively by auth-service).
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(
            Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload().getSubject()
        );
    }

    public String getRoleFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public String getPhoneFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload().get("phone", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT invalid: {}", e.getMessage());
        }
        return false;
    }
}
