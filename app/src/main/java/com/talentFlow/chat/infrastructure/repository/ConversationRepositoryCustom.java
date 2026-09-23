package com.talentFlow.chat.infrastructure.repository;

import com.talentFlow.chat.domain.Conversation;
import com.talentFlow.chat.domain.enums.ChatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepositoryCustom {
    Page<Conversation> findConversationsByUserId(UUID userId, Pageable pageable);

    Optional<Conversation> findDirectConversation(ChatType type, UUID user1Id, UUID user2Id);
}