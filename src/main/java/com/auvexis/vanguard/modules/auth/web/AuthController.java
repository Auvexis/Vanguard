package com.auvexis.vanguard.modules.auth.web;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auvexis.vanguard.modules.auth.application.AuthService;
import com.auvexis.vanguard.modules.auth.application.VerificationService;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginResponse;
import com.auvexis.vanguard.modules.auth.web.dtos.RegisterRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.TokenRefreshRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.TokenRefreshResponse;
import com.auvexis.vanguard.modules.auth.web.dtos.UserResponse;
import com.auvexis.vanguard.modules.auth.infrastructure.security.RateLimit;
import com.auvexis.vanguard.shared.web.ApiResponse;

/**
 * REST controller for public and authenticated identity operations.
 * Exposes endpoints for account lifecycle management, including registration,
 * session management, and email verification.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;

    public AuthController(AuthService authService, VerificationService verificationService) {
        this.authService = authService;
        this.verificationService = verificationService;
    }

    @RateLimit(capacity = 5, refillTokens = 5, refillDurationInMinutes = 1)
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.no_content(null);
    }

    @RateLimit(capacity = 5, refillTokens = 5, refillDurationInMinutes = 1)
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(
                "Login successful",
                authService.login(request));
    }

    /**
     * Retrieves the authenticated user's profile information.
     * Requires a valid Bearer token in the Authorization header.
     * 
     * @param email The extracted email from the security context.
     * @return The user response DTO.
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal String email) {
        return ApiResponse.ok(
                "User profile fetched successfully",
                authService.getProfile(email));
    }

    @RateLimit(capacity = 2, refillTokens = 2, refillDurationInMinutes = 1)
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(
                "Token refreshed successfuly",
                authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal String email,
            @RequestHeader("Authorization") String token) {
        authService.logout(email, token);
        return ApiResponse.no_content(null);
    }

    @RateLimit(capacity = 1, refillTokens = 1, refillDurationInMinutes = 1)
    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(
            @RequestParam("user_id") UUID userID,
            @RequestParam("email_token") String emailToken) {
        verificationService.verifyEmail(userID, emailToken);
        return ApiResponse.no_content("Email verified successfully");
    }

    @RateLimit(capacity = 1, refillTokens = 1, refillDurationInMinutes = 1)
    @PostMapping("/resend-verification-email")
    public ApiResponse<Void> resendVerificationEmail(@RequestParam("email") String email) {
        verificationService.resendVerificationEmail(email);
        return ApiResponse.no_content("Verification email sent successfully");
    }
}
