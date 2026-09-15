package com.talentFlow.auth.web.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID organizationId,
        UUID userId,
        String email,
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String message
) {
}