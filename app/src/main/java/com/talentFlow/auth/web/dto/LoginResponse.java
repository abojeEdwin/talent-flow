package com.talentFlow.auth.web.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AuthResponse user
) {
}
