package com.talentFlow.auth.web.dto;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        boolean emailVerified,
        String status
) {
}
