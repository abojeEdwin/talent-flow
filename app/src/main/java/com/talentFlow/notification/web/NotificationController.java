package com.talentFlow.notification.web;

import com.talentFlow.notification.application.NotificationReadService;
import com.talentFlow.notification.web.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INTERN','INSTRUCTOR','ADMIN')")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationReadService notificationReadService;

    @GetMapping("/")
    public ResponseEntity<Page<NotificationResponse>> listNotifications(Authentication authentication, Pageable pageable) {
        Page<NotificationResponse> notifications = notificationReadService.listNotifications(authentication, pageable);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .body(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(@PathVariable UUID notificationId, Authentication authentication) {
        return notificationReadService.markAsRead(notificationId, authentication);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        return ResponseEntity.ok(notificationReadService.markAllAsRead(authentication));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsReadPatch(Authentication authentication) {
        return ResponseEntity.ok(notificationReadService.markAllAsRead(authentication));
    }

    @RequestMapping(value = "/mark-all-read", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<Map<String, Object>> markAllAsReadLegacy(Authentication authentication) {
        log.warn("Deprecated endpoint used: /api/v1/notifications/mark-all-read. Use /api/v1/notifications/read-all instead.");
        return ResponseEntity.ok(notificationReadService.markAllAsRead(authentication));
    }
}
