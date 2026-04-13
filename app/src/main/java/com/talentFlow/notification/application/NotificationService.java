package com.talentFlow.notification.application;

import java.util.Map;
import java.util.UUID;

public interface NotificationService {
    void notifyUser(UUID userId, String type, String title, String message, Map<String, Object> payload);
}

