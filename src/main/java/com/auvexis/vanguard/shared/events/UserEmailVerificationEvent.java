package com.auvexis.vanguard.shared.events;

import java.util.UUID;

import com.auvexis.vanguard.modules.auth.domain.User;

public record UserEmailVerificationEvent(
        UUID id,
        String email,
        String name,
        String token) {
    public static UserEmailVerificationEvent from(User user, String token) {
        return new UserEmailVerificationEvent(
                user.getId(),
                user.getEmail(),
                user.getName(),
                token); 
    }
}
