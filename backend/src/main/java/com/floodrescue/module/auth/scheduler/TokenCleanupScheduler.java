package com.floodrescue.module.auth.scheduler;

import com.floodrescue.module.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    // Chạy lúc 2:00 AM mỗi ngày
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Running refresh token cleanup...");
        refreshTokenRepository.deleteExpiredAndRevoked();
        log.info("Token cleanup completed.");
    }
}