package com.auvexis.vanguard.modules.auth.application;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auvexis.vanguard.modules.auth.application.exception.RefreshTokenExpiredException;
import com.auvexis.vanguard.modules.auth.domain.RefreshToken;
import com.auvexis.vanguard.modules.auth.domain.User;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.RefreshTokenRepository;
import com.auvexis.vanguard.shared.modules.redis.RedisService;

@Service
public class JwtService {

    @Value("${auth.jwt.secret}")
    private String secret;

    @Value("${auth.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${auth.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisService redisService;

    public JwtService(RefreshTokenRepository refreshTokenRepository, RedisService redisService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.redisService = redisService;
    }

    public String generateAccessToken(User user) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("user_email", user.getEmail())
                .withClaim("role", user.getSystemRole().name())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(Algorithm.HMAC256(secret));
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        /**
         * Delete old refresh token for this user
         */
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public java.util.Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public DecodedJWT validateToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String getIdFromToken(String token) {
        DecodedJWT jwt = validateToken(token);
        return jwt != null ? jwt.getSubject() : null;
    }

    public String getEmailFromToken(String token) {
        DecodedJWT jwt = validateToken(token);
        return jwt != null ? jwt.getClaim("user_email").asString() : null;
    }

    public String getRoleFromToken(String token) {
        DecodedJWT jwt = validateToken(token);
        return jwt != null ? jwt.getClaim("role").asString() : null;
    }

    public RefreshToken verifyRefreshTokenExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteRefreshTokenByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public void addToBlackList(String token) {
        String key = "auth:tokens:blacklist:" + token;

        /**
         * Get the remaining time of the token
         */
        DecodedJWT decodedJWT = validateToken(token);
        if (decodedJWT == null) {
            return;
        }

        long remainingTime = decodedJWT.getExpiresAt().getTime()
                - System.currentTimeMillis();

        /**
         * If the token is expired, do not add it to the blacklist
         */
        if (remainingTime <= 0) {
            return;
        }

        Duration ttl = Objects.requireNonNull(Duration.ofMillis(remainingTime));

        /**
         * Value is True because we only need to check if the token exists in the
         * blacklist
         */
        redisService.set(
                key,
                true,
                ttl);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = "auth:tokens:blacklist:" + token;
        return redisService.get(key) != null;
    }
}
