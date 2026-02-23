package com.auvexis.vanguard.shared.infrastructure.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Low-level utility service for Redis operations.
 * Provides a simplified interface for common key-value operations using
 * RedisTemplate.
 * Used primarily for token blacklisting and temporary data caching.
 */
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(
            @NonNull String key,
            @NonNull Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(
            @NonNull String key,
            @NonNull Object value,
            @NonNull Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public Object get(@NonNull String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(@NonNull String key) {
        redisTemplate.delete(key);
    }

}
