package com.floodrescue.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    // Citizen và public: 100 req/min
    private static final int  PUBLIC_LIMIT    = 100;
    // Staff roles: 500 req/min
    private static final int  STAFF_LIMIT     = 500;
    private static final long WINDOW_SECONDS  = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key   = buildKey(request);
        int    limit = getLimit(request);

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // Set TTL lần đầu tạo key
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (count > limit) {
            log.warn("Rate limit exceeded for key: {}", key);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"success\":false,\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Quá nhiều request, vui lòng thử lại sau\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String buildKey(HttpServletRequest request) {
        // Ưu tiên dùng userId nếu đã auth, fallback về IP
        String userId = request.getHeader("X-User-Id");
        if (userId != null) {
            return "rate_limit:user:" + userId;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        return "rate_limit:ip:" + ip;
    }

    private int getLimit(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role != null && (role.equals("COORDINATOR")
                || role.equals("MANAGER")
                || role.equals("ADMIN"))) {
            return STAFF_LIMIT;
        }
        return PUBLIC_LIMIT;
    }
}