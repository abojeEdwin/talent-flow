package com.talentFlow.chat.infrastructure.repository;

import com.talentFlow.chat.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    @Query("""
            SELECT m FROM Message m
            WHERE m.conversation.id = :conversationId
            ORDER BY m.createdAt DESC
            """)
    List<Message> findRecentByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

    @Query("""
            SELECT m FROM Message m
            JOIN MessageReadReceipt r ON r.message = m
            WHERE m.conversation.id = :conversationId AND r.user.id = :userId
            ORDER BY m.createdAt DESC
            """)
    List<Message> findReadByConversationIdAndUserId(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            Pageable pageable);
}