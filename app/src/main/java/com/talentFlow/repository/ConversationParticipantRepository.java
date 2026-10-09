package com.talentFlow.repository;

import com.talentFlow.data.entity.ConversationParticipant;
import com.talentFlow.data.Enums.ParticipantRole;
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