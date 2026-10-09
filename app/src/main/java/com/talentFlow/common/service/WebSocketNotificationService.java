package com.talentFlow.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentFlow.auth.data.entity.User;
import com.talentFlow.repository.UserRepository;
import com.talentFlow.service.NotificationService;
import com.talentFlow.data.entity.Notification;
import com.talentFlow.repository.NotificationRepository;
import com.talentFlow.data.dto.UserNotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void notifyUser(UUID userId, String type, String title, String message, Map<String, Object> payload) {
        if (userId == null) {
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        Notification storedNotification = new Notification();
        if (user.getOrganization() == null) {
            return;
        }
        storedNotification.setOrganization(user.getOrganization());
        storedNotification.setUser(user);
        storedNotification.setType(type == null ? "INFO" : type);
        storedNotification.setTitle(title == null ? "Notification" : title);
        storedNotification.setMessage(message == null ? "" : message);
        storedNotification.setPayload(writePayload(payload));
        storedNotification.setRead(false);
        storedNotification = notificationRepository.save(storedNotification);

        Map<String, Object> safePayload = payload == null ? Map.of() : new HashMap<>(payload);
        UserNotificationMessage notification = new UserNotificationMessage(
                storedNotification.getId(),
                userId,
                storedNotification.getType(),
                storedNotification.getTitle(),
                storedNotification.getMessage(),
                safePayload,
                storedNotification.getCreatedAt()
        );
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    private String writePayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }
}
