package com.example.assistant.service.usage;

import com.example.assistant.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${usage.rate-limit-per-minute:15}")
    private int rateLimitPerMinute;

    // Fallback in-memory map if Redis is temporarily unreachable
    private final ConcurrentHashMap<String, AtomicInteger> localFallbackMap = new ConcurrentHashMap<>();

    public void checkRateLimit(Long telegramUserId) {
        String key = "ratelimit:" + telegramUserId + ":" + (System.currentTimeMillis() / 60000);
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(65));
            }
            if (count != null && count > rateLimitPerMinute) {
                log.warn("Rate limit exceeded for user {}", telegramUserId);
                throw new RateLimitExceededException("You are sending messages too quickly. Please wait a moment.");
            }
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception ex) {
            log.debug("Redis rate limiting fallback to in-memory: {}", ex.getMessage());
            AtomicInteger counter = localFallbackMap.computeIfAbsent(key, k -> new AtomicInteger(0));
            if (counter.incrementAndGet() > rateLimitPerMinute) {
                throw new RateLimitExceededException("You are sending messages too quickly. Please wait a moment.");
            }
        }
    }
}
