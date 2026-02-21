package com.auvexis.vanguard.modules.auth.web.dtos;

import java.time.Instant;
import java.util.UUID;

import com.auvexis.vanguard.modules.auth.domain.SystemRole;
import com.auvexis.vanguard.modules.auth.domain.User;

public record UserResponse(
        UUID id,
        String name,
        String email,
        SystemRole role,
        Instant created_at,
        Instant updated_at) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getSystemRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
