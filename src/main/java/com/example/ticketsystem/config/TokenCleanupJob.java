package com.example.ticketsystem.config;

import com.example.ticketsystem.repository.RefreshTokenRepository;
import com.example.ticketsystem.repository.TokenBlacklistRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Süresi dolmuş access blacklist ve refresh token kayıtlarını temizler.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int blacklisted = tokenBlacklistRepository.deleteExpiredTokens(now);
        int refresh = refreshTokenRepository.deleteAllExpired(now);
        if (blacklisted > 0 || refresh > 0) {
            log.info("Token temizliği: blacklist={}, expiredRefresh={}", blacklisted, refresh);
        }
    }
}
