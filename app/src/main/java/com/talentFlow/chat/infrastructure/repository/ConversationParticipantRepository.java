package com.talentFlow.chat.infrastructure.repository;

import com.talentFlow.chat.domain.ConversationParticipant;
import com.talentFlow.chat.domain.enums.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {

    @Query("""
            SELECT cp FROM ConversationParticipant cp
            WHERE cp.conversation.id = :conversationId
            """)
    List<ConversationParticipant> findByConversationId(@Param("conversationId") UUID conversationId);

    @Query("""
            SELECT cp FROM ConversationParticipant cp
            WHERE cp.conversation.id = :conversationId AND cp.user.id = :userId
            """)
    Optional<ConversationParticipant> findByConversationIdAndUserId(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId);

    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    @Query("""
            SELECT CASE WHEN COUNT(cp) > 0 THEN true ELSE false END
            FROM ConversationParticipant cp
            WHERE cp.conversation.id = :conversationId
              AND cp.user.id = :userId
              AND cp.role IN :roles
            """)
    boolean existsByConversationIdAndUserIdAndRoleIn(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("roles") List<ParticipantRole> roles);

    void deleteByConversationIdAndUserId(UUID conversationId, UUID userId);
}