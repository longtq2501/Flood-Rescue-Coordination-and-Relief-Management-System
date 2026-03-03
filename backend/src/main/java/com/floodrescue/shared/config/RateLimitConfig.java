package com.floodrescue.shared.config;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    // Citizen: 100 req/min
    @Bean
    public RedisRateLimiter citizenRateLimiter() {
        return new RedisRateLimiter(100, 150, 1);
        // replenishRate=100, burstCapacity=150, requestedTokens=1
    }

    // Authenticated staff: 500 req/min
    @Bean
    public RedisRateLimiter staffRateLimiter() {
        return new RedisRateLimiter(500, 600, 1);
    }

    // Public (login, register): 20 req/min per IP
    @Bean
    public RedisRateLimiter publicRateLimiter() {
        return new RedisRateLimiter(20, 40, 1);
    }
}