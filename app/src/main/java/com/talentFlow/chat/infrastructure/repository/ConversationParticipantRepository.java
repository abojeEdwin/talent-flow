package com.talentFlow.chat.infrastructure.repository;

import com.talentFlow.chat.domain.ConversationParticipant;
import com.talentFlow.chat.domain.enums.ParticipantRole;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationParticipantRepository extends MongoRepository<ConversationParticipant, UUID> {
    List<ConversationParticipant> findByConversationId(UUID conversationId);

    Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    boolean existsByConversationIdAndUserIdAndRoleIn(
            UUID conversationId,
            UUID userId,
            List<ParticipantRole> roles
    );

    void deleteByConversationIdAndUserId(UUID conversationId, UUID userId);
}