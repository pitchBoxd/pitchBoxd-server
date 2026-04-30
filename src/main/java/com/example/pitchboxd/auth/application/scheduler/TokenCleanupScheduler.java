package com.example.pitchboxd.auth.application.scheduler;

import com.example.pitchboxd.auth.infrastructure.RefreshTokenRepository;
import com.example.pitchboxd.global.domain.ClockHolder;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final ClockHolder clockHolder;

    // 새벽 3시에 만료된 리프레시 토큰 삭제
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = clockHolder.now();
        refreshTokenRepository.deleteAllExpiredSince(now);
    }
}
