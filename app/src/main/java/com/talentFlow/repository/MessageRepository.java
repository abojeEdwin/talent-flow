package com.talentFlow.repository;

import com.talentFlow.data.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends MongoRepository<Message, UUID> {
    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    List<Message> findTop100ByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
}