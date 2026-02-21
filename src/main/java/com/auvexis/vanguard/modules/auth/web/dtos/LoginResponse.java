package com.auvexis.vanguard.modules.auth.web.dtos;

public record LoginResponse(
        String access_token,
        String refresh_token) {
}
