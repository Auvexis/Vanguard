package com.auvexis.vanguard.modules.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auvexis.vanguard.modules.auth.domain.RefreshToken;
import com.auvexis.vanguard.modules.auth.domain.User;
import com.auvexis.vanguard.modules.auth.domain.SystemRole;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.RefreshTokenRepository;
import com.auvexis.vanguard.shared.modules.redis.RedisService;

/**
 * Unit tests for JwtService.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RedisService redisService;

    private JwtService jwtService;

    private final String secret = "testSecret";
    private final long jwtExpirationMs = 3600000; // 1 hour
    private final long refreshExpirationMs = 86400000; // 1 day

    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(refreshTokenRepository, redisService);
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", jwtExpirationMs);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", refreshExpirationMs);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setSystemRole(SystemRole.SYSTEM_ADMIN);
    }

    /**
     * Test successful access token generation.
     */
    @Test
    void testGenerateAccessToken() {
        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);

        assertEquals(user.getId().toString(), decodedJWT.getSubject());
        assertEquals(user.getEmail(), decodedJWT.getClaim("user_email").asString());
        assertEquals(user.getSystemRole().name(), decodedJWT.getClaim("role").asString());
    }

    /**
     * Test successful refresh token creation.
     */
    @Test
    void testCreateRefreshToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken refreshToken = jwtService.createRefreshToken(user);

        assertNotNull(refreshToken);
        assertEquals(user, refreshToken.getUser());
        assertNotNull(refreshToken.getToken());
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    /**
     * Test token validation with valid token.
     */
    @Test
    void testValidateToken_Valid() {
        String token = jwtService.generateAccessToken(user);
        DecodedJWT decodedJWT = jwtService.validateToken(token);
        assertNotNull(decodedJWT);
        assertEquals(user.getId().toString(), decodedJWT.getSubject());
    }

    /**
     * Test token validation with invalid secret.
     */
    @Test
    void testValidateToken_InvalidSecret() {
        String token = JWT.create()
                .withSubject(user.getId().toString())
                .sign(Algorithm.HMAC256("wrongSecret"));

        DecodedJWT decodedJWT = jwtService.validateToken(token);
        assertNull(decodedJWT);
    }

    /**
     * Test token validation with expired token.
     */
    @Test
    void testValidateToken_Expired() {
        String token = JWT.create()
                .withSubject(user.getId().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() - 10000))
                .sign(Algorithm.HMAC256(secret));

        DecodedJWT decodedJWT = jwtService.validateToken(token);
        assertNull(decodedJWT);
    }

    /**
     * Test adding valid token to blacklist.
     */
    @Test
    void testAddToBlackList_ValidToken() {
        String token = jwtService.generateAccessToken(user);

        jwtService.addToBlackList(token);

        verify(redisService).set(eq("auth:tokens:blacklist:" + token), eq(true), any(Duration.class));
    }

    /**
     * Test adding invalid token to blacklist (should return early without errors).
     */
    @Test
    void testAddToBlackList_InvalidToken() {
        String token = "invalid.token.here";

        jwtService.addToBlackList(token);

        verify(redisService, never()).set(any(), any(), any());
    }

    /**
     * Test checking if token is blacklisted.
     */
    @Test
    void testIsTokenBlacklisted() {
        String token = "someToken";
        when(redisService.get("auth:tokens:blacklist:" + token)).thenReturn(true);

        boolean result = jwtService.isTokenBlacklisted(token);

        assertTrue(result);
        verify(redisService).get("auth:tokens:blacklist:" + token);
    }
}
