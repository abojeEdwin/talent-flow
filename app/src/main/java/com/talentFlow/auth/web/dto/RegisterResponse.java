package com.talentFlow.auth.web.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String message
) {
}
