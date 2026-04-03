package com.talentFlow.admin.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserSummaryResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String status,
        boolean emailVerified,
        String role,
        LocalDateTime lastLoginAt
) {
}
