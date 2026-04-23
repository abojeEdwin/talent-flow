package com.talentFlow.chat.infrastructure.websocket;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.chat.infrastructure.repository.ConversationParticipantRepository;
import com.talentFlow.chat.infrastructure.repository.ConversationRepository;
import com.talentFlow.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.typing")
    public void handleTyping(
            @DestinationVariable("conversationId") UUID conversationId,
            TypingMessage typingMessage,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        if (!participantRepository.existsByConversationIdAndUserId(conversationId, user.getId())) {
            log.warn("User {} attempted to send typing indicator to non-member conversation {}",
                    user.getId(), conversationId);
            return;
        }

        TypingPayload payload = new TypingPayload(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                typingMessage.isTyping()
        );

        messagingTemplate.convertAndSend("/topic/chat/" + conversationId + "/typing", payload);
    }

    @MessageMapping("/chat.send")
    public void handleSend(
            @DestinationVariable("conversationId") UUID conversationId,
            WebSocketMessage wsMessage,
            Authentication authentication
    ) {
        // Handled via REST API - this is just for validation
        log.debug("WebSocket send ignored, use REST API for sending messages");
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public record TypingMessage(boolean isTyping) {}

    public record WebSocketMessage(String content) {}

    public record TypingPayload(UUID userId, String firstName, String lastName, boolean isTyping) {}
}