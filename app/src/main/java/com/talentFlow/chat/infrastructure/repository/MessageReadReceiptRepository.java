package com.talentFlow.chat.infrastructure.repository;

import com.talentFlow.chat.domain.MessageReadReceipt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageReadReceiptRepository extends MongoRepository<MessageReadReceipt, UUID> {
    List<MessageReadReceipt> findByMessageId(UUID messageId);

    Optional<MessageReadReceipt> findByMessageIdAndUserId(UUID messageId, UUID userId);

    boolean existsByMessageIdAndUserId(UUID messageId, UUID userId);
}