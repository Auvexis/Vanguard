package com.auvexis.vanguard.modules.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auvexis.vanguard.modules.auth.application.exception.EmailAlreadyInUseException;
import com.auvexis.vanguard.modules.auth.application.exception.EmailNotVerifiedException;
import com.auvexis.vanguard.modules.auth.application.exception.InvalidCredentialsException;
import com.auvexis.vanguard.modules.auth.domain.RefreshToken;
import com.auvexis.vanguard.modules.auth.domain.User;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.EmailVerificationRepository;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.UserRepository;
import com.auvexis.vanguard.modules.auth.messaging.UserPublisher;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginResponse;
import com.auvexis.vanguard.modules.auth.web.dtos.RegisterRequest;
import com.auvexis.vanguard.shared.events.UserEmailVerificationEvent;
import com.auvexis.vanguard.shared.infrastructure.jwt.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository repo;
    @Mock
    private PasswordEncoder pwdEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserPublisher userPublisher;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(repo, pwdEncoder, jwtService, userPublisher, emailVerificationRepository);
    }

    @Test
    void register_ShouldSaveUserAndPublishEvent() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(repo.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(pwdEncoder.encode(anyString())).thenReturn("hashedPassword");

        authService.register(request);

        verify(repo).save(any(User.class));
        verify(emailVerificationRepository).save(any());
        verify(userPublisher).publishUserEmailVerification(any(UserEmailVerificationEvent.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyInUse() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        when(repo.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyInUseException.class, () -> authService.register(request));
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("Test", "test@example.com", "hashedPassword");
        user.setEmailVerified(true);

        when(repo.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(pwdEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refreshToken");
        when(jwtService.createRefreshToken(user)).thenReturn(refreshToken);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.access_token());
        assertEquals("refreshToken", response.refresh_token());
        verify(jwtService).deleteRefreshTokenByUser(user);
    }

    @Test
    void login_ShouldThrowException_WhenEmailNotVerified() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("Test", "test@example.com", "hashedPassword");
        user.setEmailVerified(false);

        when(repo.findByEmail(request.email())).thenReturn(Optional.of(user));

        assertThrows(EmailNotVerifiedException.class, () -> authService.login(request));
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsInvalid() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        User user = new User("Test", "test@example.com", "hashedPassword");
        user.setEmailVerified(true);

        when(repo.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(pwdEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
