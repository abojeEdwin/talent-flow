package com.talentFlow.data.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AuthResponse user
) {
}
