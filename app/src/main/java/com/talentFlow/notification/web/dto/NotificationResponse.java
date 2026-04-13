package com.talentFlow.notification.web.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String message,
        Map<String, Object> payload,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
