package com.auvexis.vanguard.shared.modules.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Unit tests for RedisService.
 */
@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisService redisService;

    @BeforeEach
    void setUp() {
        redisService = new RedisService(redisTemplate);
    }

    /**
     * Test of set method without TTL.
     */
    @Test
    void testSet_withoutTtl() {
        String key = "testKey";
        String value = "testValue";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.set(key, value);

        verify(valueOperations).set(key, value);
    }

    /**
     * Test of set method with TTL.
     */
    @Test
    void testSet_withTtl() {
        String key = "testKey";
        String value = "testValue";
        Duration ttl = Duration.ofMinutes(5);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisService.set(key, value, ttl);

        verify(valueOperations).set(key, value, ttl);
    }

    /**
     * Test of get method.
     */
    @Test
    void testGet() {
        String key = "testKey";
        String expectedValue = "testValue";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(expectedValue);

        Object actualValue = redisService.get(key);

        assertEquals(expectedValue, actualValue);
        verify(valueOperations).get(key);
    }

    /**
     * Test of delete method.
     */
    @Test
    void testDelete() {
        String key = "testKey";

        redisService.delete(key);

        verify(redisTemplate).delete(key);
    }
}
