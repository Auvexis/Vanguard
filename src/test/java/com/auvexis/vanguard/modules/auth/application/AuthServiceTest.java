package com.auvexis.vanguard.modules.auth.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auvexis.vanguard.modules.auth.application.exception.EmailAlreadyInUseException;
import com.auvexis.vanguard.modules.auth.application.exception.InvalidCredentialsException;
import com.auvexis.vanguard.modules.auth.application.exception.RefreshTokenExpiredException;
import com.auvexis.vanguard.modules.auth.domain.RefreshToken;
import com.auvexis.vanguard.modules.auth.domain.User;
import com.auvexis.vanguard.modules.auth.infrastructure.repository.UserRepository;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginResponse;
import com.auvexis.vanguard.modules.auth.web.dtos.RegisterRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.TokenRefreshRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.TokenRefreshResponse;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    /**
     * Test successful user registration.
     */
    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    /**
     * Test registration with an email already in use.
     */
    @Test
    void testRegister_EmailAlreadyInUse() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyInUseException.class, () -> authService.register(request));
    }

    /**
     * Test successful user login.
     */
    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User();
        user.setPassword("encodedPassword");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("mockRefreshToken");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("mockAccessToken");
        when(jwtService.createRefreshToken(user)).thenReturn(refreshToken);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockAccessToken", response.access_token());
        assertEquals("mockRefreshToken", response.refresh_token());
        verify(jwtService).deleteRefreshTokenByUser(user);
    }

    /**
     * Test login with invalid credentials (wrong password).
     */
    @Test
    void testLogin_InvalidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User();
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    /**
     * Test user logout with Bearer token.
     */
    @Test
    void testLogout_Success() {
        String email = "test@example.com";
        String token = "Bearer someToken";
        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        authService.logout(email, token);

        verify(jwtService).deleteRefreshTokenByUser(user);
        verify(jwtService).addToBlackList("someToken");
    }

    /**
     * Test token refresh successfully.
     */
    @Test
    void testRefreshToken_Success() {
        TokenRefreshRequest request = new TokenRefreshRequest("validRefreshToken");
        RefreshToken refreshToken = new RefreshToken();
        User user = new User();
        refreshToken.setUser(user);

        RefreshToken newRefreshTokenObj = new RefreshToken();
        newRefreshTokenObj.setToken("newRefreshToken");

        when(jwtService.findByToken("validRefreshToken")).thenReturn(Optional.of(refreshToken));
        when(jwtService.verifyRefreshTokenExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtService.generateAccessToken(user)).thenReturn("newAccessToken");
        when(jwtService.createRefreshToken(user)).thenReturn(newRefreshTokenObj);

        TokenRefreshResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.access_token());
        assertEquals("newRefreshToken", response.refresh_token());
        verify(jwtService).deleteRefreshTokenByUser(user);
    }

    /**
     * Test token refresh with invalid/expired refresh token.
     */
    @Test
    void testRefreshToken_Failure() {
        TokenRefreshRequest request = new TokenRefreshRequest("invalidRefreshToken");
        when(jwtService.findByToken("invalidRefreshToken")).thenReturn(Optional.empty());

        assertThrows(RefreshTokenExpiredException.class, () -> authService.refreshToken(request));
    }
}
