package com.auvexis.vanguard.shared.modules.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

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
