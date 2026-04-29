package com.talentFlow.notification.web;

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
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INTERN','INSTRUCTOR','ADMIN')")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/")
    public ResponseEntity<Page<NotificationResponse>> listNotifications(Authentication authentication, Pageable pageable) {
        User actor = getActor(authentication);
        Page<NotificationResponse> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(actor, pageable)
                .map(this::toResponse);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .body(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    @Transactional
    public NotificationResponse markAsRead(@PathVariable UUID notificationId, Authentication authentication) {
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

    @PutMapping("/read-all")
    @Transactional
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        User actor = getActor(authentication);
        LocalDateTime now = LocalDateTime.now();
        
        // Find all unread notifications for the user
        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadFalse(actor);
        
        // Mark them all as read
        int updatedCount = 0;
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(now);
            updatedCount++;
        }
        
        if (updatedCount > 0) {
            notificationRepository.saveAll(unreadNotifications);
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "All notifications marked as read",
            "count", updatedCount
        ));
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
