package com.example.ticketsystem.security;

import com.example.ticketsystem.exception.TooManyRequestsException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void check(String clientKey) {
        Instant now = Instant.now();
        AttemptWindow window = attempts.get(clientKey);
        if (window != null) {
            if (Duration.between(window.startedAt(), now).compareTo(WINDOW) < 0) {
                if (window.count() >= MAX_ATTEMPTS) {
                    throw new TooManyRequestsException(
                            "Çok fazla başarısız giriş denemesi. Lütfen bir dakika sonra tekrar deneyin");
                }
            } else {
                attempts.remove(clientKey);
            }
        }
    }

    public void recordFailure(String clientKey) {
        Instant now = Instant.now();
        attempts.compute(clientKey, (key, current) -> {
            if (current == null || Duration.between(current.startedAt(), now).compareTo(WINDOW) >= 0) {
                return new AttemptWindow(now, 1);
            }
            return new AttemptWindow(current.startedAt(), current.count() + 1);
        });
    }

    public void reset(String clientKey) {
        attempts.remove(clientKey);
    }

    @Scheduled(fixedDelay = 300000)
    public void removeExpiredWindows() {
        Instant threshold = Instant.now().minus(WINDOW);
        attempts.entrySet().removeIf(entry -> entry.getValue().startedAt().isBefore(threshold));
    }

    private record AttemptWindow(Instant startedAt, int count) {
    }
}