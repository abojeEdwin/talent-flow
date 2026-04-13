package com.talentFlow.notification.infrastructure.websocket;

import com.talentFlow.notification.application.NotificationService;
import com.talentFlow.notification.web.dto.UserNotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyUser(UUID userId, String type, String title, String message, Map<String, Object> payload) {
        if (userId == null) {
            return;
        }

        UserNotificationMessage notification = new UserNotificationMessage(
                UUID.randomUUID(),
                userId,
                type,
                title,
                message,
                payload,
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }
}

