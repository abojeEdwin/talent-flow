package com.talentFlow.notification.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.notification.domain.Notification;
import com.talentFlow.notification.infrastructure.repository.NotificationRepository;
import com.talentFlow.notification.web.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationReadServiceImpl implements NotificationReadService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Page<NotificationResponse> listNotifications(Authentication authentication, Pageable pageable) {
        User actor = getActor(authentication);
        return notificationRepository.findByUserOrderByCreatedAtDesc(actor, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, Authentication authentication) {
        User actor = getActor(authentication);
        Notification notification = notificationRepository.findByIdAndUser(notificationId, actor)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Override
    @Transactional
    public Map<String, Object> markAllAsRead(Authentication authentication) {
        User actor = getActor(authentication);
        LocalDateTime now = LocalDateTime.now();

        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadFalse(actor);

        int updatedCount = 0;
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(now);
            updatedCount++;
        }

        if (updatedCount > 0) {
            notificationRepository.saveAll(unreadNotifications);
        }

        return Map.of(
                "message", "All notifications marked as read",
                "count", updatedCount
        );
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                parsePayload(notification.getPayload()),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    private Map<String, Object> parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return Map.of("raw", payload);
        }
    }

    private User getActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}

