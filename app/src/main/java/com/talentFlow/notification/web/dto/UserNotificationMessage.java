package com.talentFlow.notification.web.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record UserNotificationMessage(
        UUID id,
        UUID userId,
        String type,
        String title,
        String message,
        Map<String, Object> payload,
        LocalDateTime createdAt
) {
}

