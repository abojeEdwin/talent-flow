package com.talentFlow.notification.application;

import com.talentFlow.notification.web.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

public interface NotificationReadService {
    Page<NotificationResponse> listNotifications(Authentication authentication, Pageable pageable);

    NotificationResponse markAsRead(UUID notificationId, Authentication authentication);

    Map<String, Object> markAllAsRead(Authentication authentication);
}

