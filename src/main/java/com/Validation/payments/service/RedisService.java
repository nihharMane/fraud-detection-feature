package com.Validation.payments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // Existing duplicate-transaction storage
    public void save(String key, String value) {
        redisTemplate.opsForValue().set(
                key,
                value,
                24,
                TimeUnit.HOURS
        );
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Add a transaction to the user's velocity window
    public long addTransactionAndCount(String key, String transactionId) {

        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60_000;

        // Add transaction with timestamp as score
        redisTemplate.opsForZSet().add(
                key,
                transactionId,
                currentTime
        );

        // Remove transactions older than 60 seconds
        redisTemplate.opsForZSet().removeRangeByScore(
                key,
                0,
                oneMinuteAgo
        );

        // Count transactions remaining in the 60-second window
        Long count = redisTemplate.opsForZSet().zCard(key);

        return count != null ? count : 0;
    }
}