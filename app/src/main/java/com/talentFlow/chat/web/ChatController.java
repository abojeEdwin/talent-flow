package com.talentFlow.chat.web;

import com.talentFlow.chat.application.ChatService;
import com.talentFlow.chat.web.dto.*;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @GetMapping("/users/search")
    public Page<SearchUserResponse> searchUsers(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return chatService.searchUsers(q, pageable);
    }

    @PostMapping("/conversations/direct/{otherUserId}")
    public ResponseEntity<ConversationResponse> createDirectConversation(
            @PathVariable UUID otherUserId,
            Authentication authentication
    ) {
        User creator = getAuthenticatedUser(authentication);
        ConversationResponse response = chatService.createDirectConversation(otherUserId, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> createGroupConversation(
            @Valid @RequestBody CreateConversationRequest request,
            Authentication authentication
    ) {
        User creator = getAuthenticatedUser(authentication);
        ConversationResponse response = chatService.createGroupConversation(request, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversations")
    public Page<ConversationResponse> listConversations(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User user = getAuthenticatedUser(authentication);
        return chatService.getUserConversations(user, pageable);
    }

    @GetMapping("/conversations/{conversationId}")
    public ConversationResponse getConversation(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        return chatService.getConversation(conversationId, user);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public Page<MessageResponse> getMessages(
            @PathVariable UUID conversationId,
            Authentication authentication,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        User user = getAuthenticatedUser(authentication);
        return chatService.getMessages(conversationId, user, pageable);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public MessageResponse sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {
        User sender = getAuthenticatedUser(authentication);
        return chatService.sendMessage(conversationId, sender, request);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);
        chatService.markAsRead(conversationId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations/{conversationId}/read")
    public ReadReceiptResponse getReadReceipts(
            @PathVariable UUID conversationId,
            @RequestParam UUID messageId
    ) {
        return chatService.getReadReceipts(conversationId, messageId);
    }

    @PostMapping("/conversations/{conversationId}/participants")
    public ResponseEntity<Void> addParticipants(
            @PathVariable UUID conversationId,
            @Valid @RequestBody AddParticipantRequest request,
            Authentication authentication
    ) {
        User creator = getAuthenticatedUser(authentication);
        chatService.addParticipants(conversationId, request, creator);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/conversations/{conversationId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable UUID conversationId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        User remover = getAuthenticatedUser(authentication);
        chatService.removeParticipant(conversationId, userId, remover);
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}