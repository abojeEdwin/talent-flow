package com.talentFlow.chat.infrastructure.repository;

import com.talentFlow.chat.domain.MessageReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageReadReceiptRepository extends JpaRepository<MessageReadReceipt, UUID> {

    @Query("""
            SELECT r FROM MessageReadReceipt r
            WHERE r.message.id = :messageId
            """)
    List<MessageReadReceipt> findByMessageId(@Param("messageId") UUID messageId);

    Optional<MessageReadReceipt> findByMessageIdAndUserId(UUID messageId, UUID userId);

    boolean existsByMessageIdAndUserId(UUID messageId, UUID userId);

    @Query("""
            SELECT r FROM MessageReadReceipt r
            WHERE r.message.conversation.id = :conversationId
              AND r.user.id = :userId
            ORDER BY r.readAt DESC
            """)
    List<MessageReadReceipt> findByConversationIdAndUserIdOrderByReadAtDesc(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId);
}