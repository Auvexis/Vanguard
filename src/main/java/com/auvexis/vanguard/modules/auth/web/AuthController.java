package com.auvexis.vanguard.modules.auth.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auvexis.vanguard.modules.auth.application.AuthService;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.LoginResponse;
import com.auvexis.vanguard.modules.auth.web.dtos.RegisterRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.TokenRefreshRequest;
import com.auvexis.vanguard.modules.auth.web.dtos.TokenRefreshResponse;
import com.auvexis.vanguard.modules.auth.web.dtos.UserResponse;
import com.auvexis.vanguard.shared.web.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.no_content(null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(
                "Login successful",
                authService.login(request));
    }

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

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal String email) {
        return ApiResponse.ok(
                "User profile fetched successfully",
                authService.getProfile(email));
    }
}
