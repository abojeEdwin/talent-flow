package com.talentFlow.notification.web;

import com.talentFlow.notification.application.NotificationReadService;
import com.talentFlow.notification.web.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
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

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INTERN','INSTRUCTOR','ADMIN')")
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "Bearer JWT")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private static final String DEPRECATION_SUNSET = "2026-08-31";

    private final NotificationReadService notificationReadService;

    @GetMapping("/")
    @Operation(summary = "List user notifications", description = "Retrieve paginated list of notifications for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT")
    })
    public ResponseEntity<Page<NotificationResponse>> listNotifications(Authentication authentication, Pageable pageable) {
        Page<NotificationResponse> notifications = notificationReadService.listNotifications(authentication, pageable);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .body(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark single notification as read", description = "Mark a specific notification as read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public NotificationResponse markAsRead(@PathVariable UUID notificationId, Authentication authentication) {
        return notificationReadService.markAsRead(notificationId, authentication);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read (PUT)", description = "Mark all unread notifications as read using PUT method")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        return ResponseEntity.ok(notificationReadService.markAllAsRead(authentication));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read (PATCH)", description = "Mark all unread notifications as read using PATCH method")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> markAllAsReadPatch(Authentication authentication) {
        return ResponseEntity.ok(notificationReadService.markAllAsRead(authentication));
    }

    @RequestMapping(value = "/mark-all-read", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Mark all notifications as read (LEGACY)", description = "DEPRECATED: Use /read-all instead. Mark all unread notifications as read.")
    @Deprecated(since = "1.0", forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All notifications marked as read with deprecation headers"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> markAllAsReadLegacy(Authentication authentication) {
        log.warn("Deprecated endpoint used: /api/v1/notifications/mark-all-read. Use /api/v1/notifications/read-all instead.");
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .header("Sunset", DEPRECATION_SUNSET)
                .header("Warning", "299 - \"Deprecated endpoint /mark-all-read. Use /read-all instead.\"")
                .body(notificationReadService.markAllAsRead(authentication));
    }
}
