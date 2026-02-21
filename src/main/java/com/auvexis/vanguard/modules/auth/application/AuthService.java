package com.auvexis.vanguard.modules.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.auvexis.vanguard.modules.auth.web.dtos.UserResponse;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder pwdEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repo, PasswordEncoder pwdEncoder, JwtService jwtService) {
        this.repo = repo;
        this.pwdEncoder = pwdEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        repo.findByEmail(request.email())
                .ifPresent(user -> {
                    throw new EmailAlreadyInUseException(request.email());
                });

        repo.save(new User(
                request.name(),
                request.email(),
                pwdEncoder.encode(request.password())));
    }

    public LoginResponse login(LoginRequest request) {
        User user = repo.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

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

    public void logout(String email, String token) {
        repo.findByEmail(email)
                .ifPresent(user -> {
                    String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
                    jwtService.deleteRefreshTokenByUser(user);
                    jwtService.addToBlackList(pureToken);
                });
    }

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

    public UserResponse getProfile(String email) {
        return repo.findByEmail(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new InvalidCredentialsException());
    }
}
