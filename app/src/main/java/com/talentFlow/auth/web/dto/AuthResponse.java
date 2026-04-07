package com.talentFlow.auth.web.dto;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        String status
) {
}
