package com.talentFlow.admin.web.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record AdminUserDetailResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String status,
        boolean emailVerified,
        int failedLoginAttempts,
        LocalDateTime lockedUntil,
        LocalDateTime lastLoginAt,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
