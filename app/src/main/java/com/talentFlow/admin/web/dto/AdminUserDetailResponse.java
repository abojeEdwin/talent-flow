package com.talentFlow.admin.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserDetailResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String status,
        int failedLoginAttempts,
        LocalDateTime lockedUntil,
        LocalDateTime lastLoginAt,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
