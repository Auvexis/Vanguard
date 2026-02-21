package com.auvexis.vanguard.modules.auth.web.dtos;

public record RegisterRequest(
                String name,
                String email,
                String password) {
}
