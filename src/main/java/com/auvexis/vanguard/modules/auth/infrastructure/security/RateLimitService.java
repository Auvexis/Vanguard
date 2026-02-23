package com.auvexis.vanguard.modules.auth.infrastructure.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Component
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, long capacity, long refillTokens, int refillDurationInMinutes) {
        return buckets.computeIfAbsent(key, k -> newBucket(capacity, refillTokens, refillDurationInMinutes));
    }

    private Bucket newBucket(long capacity, long refillTokens, int refillDurationInMinutes) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity,
                        Refill.intervally(refillTokens, Duration.ofMinutes(refillDurationInMinutes))))
                .build();
    }

    public boolean tryConsume(String key, long capacity, long refillTokens, int refillDurationInMinutes) {
        Bucket bucket = resolveBucket(key, capacity, refillTokens, refillDurationInMinutes);
        return bucket.tryConsume(1);
    }

}
