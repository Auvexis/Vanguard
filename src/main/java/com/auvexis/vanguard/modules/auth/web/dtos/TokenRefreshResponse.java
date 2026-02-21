package com.auvexis.vanguard.modules.auth.web.dtos;

public record TokenRefreshResponse(
                String access_token,
                String refresh_token) {
}
