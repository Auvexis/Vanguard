package com.auvexis.vanguard.modules.auth.application;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auvexis.vanguard.modules.auth.application.exception.EmailAlreadyInUseException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailNotVerifiedException;
import com.auvexis.vanguard.modules.auth.application.exception.InvalidCredentialsException;
import com.auvexis.vanguard.modules.auth.application.exception.RefreshTokenExpiredException;
import com.auvexis.vanguard.modules.auth.domain.EmailVerification;
import com.auvexis.vanguard.modules.auth.domain.RefreshToken;
import com.auvexis.vanguard.modules.auth.domain.User;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.EmailVerificationRepository;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.UserRepository;
import com.auvexis.vanguard.modules.auth.messaging.UserPublisher;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginResponse;
import com.auvexis.vanguard.modules.auth.web.dtos.RegisterRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.TokenRefreshRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.TokenRefreshResponse;
import com.auvexis.vanguard.modules.auth.web.dtos.UserResponse;
import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;
import com.auvexis.vanguard.shared.infrastructure.jwt.JwtService;

import jakarta.transaction.Transactional;

/**
 * Core application service for authentication and user management.
 * Coordinates user registration, login security, account logout, and token
 * refresh operations.
 * Integrates with messaging infrastructure for asynchronous email verification
 * triggers.
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder pwdEncoder;
    private final JwtService jwtService;
    private final UserPublisher userPublisher;
    private final EmailVerificationRepository emailVerificationRepository;

    public AuthService(
            UserRepository repo,
            PasswordEncoder pwdEncoder,
            JwtService jwtService,
            UserPublisher userPublisher,
            EmailVerificationRepository emailVerificationRepository) {
        this.repo = repo;
        this.pwdEncoder = pwdEncoder;
        this.jwtService = jwtService;
        this.userPublisher = userPublisher;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    /**
     * Registers a new user in the system.
     * Prevents duplicate emails, hashes passwords for security, and initializes the
     * email
     * verification process by persisting a verification token and publishing a
     * verification event.
     * 
     * @param request The registration details containing name, email, and password.
     */
    public void register(RegisterRequest request) {
        repo.findByEmail(request.email())
                .ifPresent(user -> {
                    throw new EmailAlreadyInUseException(request.email());
                });

        User user = repo.save(new User(
                request.name(),
                request.email(),
                pwdEncoder.encode(request.password())));

        EmailVerification emailVerification = new EmailVerification();

        String emailToken = pwdEncoder.encode(emailVerification.generateVerificationToken());
        emailVerification.setTokenHash(emailToken);
        emailVerification.setUser(user);

        emailVerificationRepository.save(emailVerification);

        userPublisher
                .publishUserEmailVerification(UserEmailVerificationEvent.from(
                        user,
                        emailToken));
    }

    /**
     * Authenticates a user based on credentials.
     * Validates email verification status and password hash.
     * Upon success, invalidates existing refresh tokens and issues a new pair of
     * access/refresh tokens.
     * 
     * @param request The login credentials.
     * @return A LoginResponse containing the new JWT access and refresh tokens.
     */
    public LoginResponse login(LoginRequest request) {
        User user = repo.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        if (!pwdEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        /**
         * Revoke all existing refresh tokens for this user
         */
        jwtService.deleteRefreshTokenByUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = jwtService.createRefreshToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken.getToken());
    }

    @CacheEvict(value = "auth:user:profile", key = "#email")
    public void logout(String email, String token) {
        repo.findByEmail(email)
                .ifPresent(user -> {
                    jwtService.removeTokenAccess(user, token);
                });
    }

    @Cacheable(value = "auth:user:profile", key = "#email")
    public UserResponse getProfile(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException());

        return UserResponse.from(user);
    }

    /**
     * Exchanges a valid refresh token for a new set of access and refresh tokens.
     * Implements token rotation by invalidating the old refresh token and issuing a
     * new one.
     * 
     * @param request The refresh token provided by the client.
     * @return A new pair of access and refresh tokens.
     */
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.refresh_token();

        return jwtService.findByToken(requestRefreshToken)
                .map(jwtService::verifyRefreshTokenExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    jwtService.deleteRefreshTokenByUser(user);

                    String accessToken = jwtService.generateAccessToken(user);
                    String newRefreshToken = jwtService.createRefreshToken(user).getToken();
                    return new TokenRefreshResponse(accessToken, newRefreshToken);
                })
                .orElseThrow(() -> new RefreshTokenExpiredException("Refresh token is not in database!"));
    }
}
