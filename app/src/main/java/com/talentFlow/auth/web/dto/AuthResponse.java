package com.talentFlow.auth.web.dto;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        UUID organizationId,
        String email,
        String firstName,
        String lastName,
        String role,
        String status
) {
}
