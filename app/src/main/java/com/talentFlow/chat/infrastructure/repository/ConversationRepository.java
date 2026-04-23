package com.talentFlow.chat.infrastructure.repository;

import com.talentFlow.chat.domain.Conversation;
import com.talentFlow.chat.domain.enums.ChatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            SELECT c FROM Conversation c
            JOIN ConversationParticipant cp ON cp.conversation = c
            WHERE cp.user.id = :userId
            ORDER BY c.updatedAt DESC
            """)
    Page<Conversation> findConversationsByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT c FROM Conversation c
            JOIN ConversationParticipant cp1 ON cp1.conversation = c
            JOIN ConversationParticipant cp2 ON cp2.conversation = c
            WHERE c.type = :type
              AND cp1.user.id = :user1Id
              AND cp2.user.id = :user2Id
            """)
    Optional<Conversation> findDirectConversation(
            @Param("type") ChatType type,
            @Param("user1Id") UUID user1Id,
            @Param("user2Id") UUID user2Id);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.type = 'COHORT_CHAT' AND c.cohort.id = :cohortId
            """)
    Optional<Conversation> findByCohortId(@Param("cohortId") UUID cohortId);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.type = 'TEAM_CHAT' AND c.team.id = :teamId
            """)
    Optional<Conversation> findByTeamId(@Param("teamId") UUID teamId);

    boolean existsByIdAndCreatedById(UUID conversationId, UUID userId);
}