package com.talentFlow.chat.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.chat.web.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ChatService {

    Page<SearchUserResponse> searchUsers(String query, Pageable pageable);

    ConversationResponse createDirectConversation(UUID otherUserId, User creator);

    ConversationResponse createGroupConversation(CreateConversationRequest request, User creator);

    Page<ConversationResponse> getUserConversations(User user, Pageable pageable);

    ConversationResponse getConversation(UUID conversationId, User user);

    Page<MessageResponse> getMessages(UUID conversationId, User user, Pageable pageable);

    MessageResponse sendMessage(UUID conversationId, User sender, SendMessageRequest request);

    void markAsRead(UUID conversationId, User user);

    ReadReceiptResponse getReadReceipts(UUID conversationId, UUID messageId);

    void addParticipants(UUID conversationId, AddParticipantRequest request, User creator);

    void removeParticipant(UUID conversationId, UUID userId, User remover);
}