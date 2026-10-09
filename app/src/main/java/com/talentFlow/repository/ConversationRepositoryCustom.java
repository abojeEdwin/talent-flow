package com.talentFlow.repository;

import com.talentFlow.data.entity.Conversation;
import com.talentFlow.data.Enums.ChatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepositoryCustom {
    Page<Conversation> findConversationsByUserId(UUID userId, Pageable pageable);

    Optional<Conversation> findDirectConversation(ChatType type, UUID user1Id, UUID user2Id);
}