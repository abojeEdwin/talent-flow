package com.talentFlow.chat.infrastructure.repository;

import com.talentFlow.chat.domain.Conversation;
import com.talentFlow.chat.domain.enums.ChatType;
import com.talentFlow.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepositoryCustom {

    private static final String PARTICIPANTS_COLLECTION = "chat_conversation_participants";
    private static final String CONVERSATIONS_COLLECTION = "chat_conversations";

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Conversation> findConversationsByUserId(UUID userId, Pageable pageable) {
        List<Object> conversationIds = findConversationIdsForUser(userId, null);

        Query conversationQuery = new Query()
                .addCriteria(Criteria.where("_id").in(conversationIds))
                .with(pageable);
        applyTenantScope(conversationQuery);

        List<Conversation> conversations = mongoTemplate.find(conversationQuery, Conversation.class);
        return new PageImpl<>(conversations, pageable, conversationIds.size());
    }

    @Override
    public Optional<Conversation> findDirectConversation(ChatType type, UUID user1Id, UUID user2Id) {
        List<Object> user1Ids = findConversationIdsForUser(user1Id, null);
        List<Object> user2Ids = findConversationIdsForUser(user2Id, null);
        if (user1Ids.isEmpty() || user2Ids.isEmpty()) {
            return Optional.empty();
        }

        List<Object> commonIds = new ArrayList<>(user1Ids);
        commonIds.retainAll(user2Ids);
        if (commonIds.isEmpty()) {
            return Optional.empty();
        }

        Query conversationQuery = new Query()
                .addCriteria(Criteria.where("_id").in(commonIds))
                .addCriteria(Criteria.where("type").is(type.name()));
        applyTenantScope(conversationQuery);

        return mongoTemplate.find(conversationQuery, Conversation.class).stream().findFirst();
    }

    private List<Object> findConversationIdsForUser(UUID userId, UUID tenantId) {
        Query participantQuery = new Query()
                .addCriteria(Criteria.where("user.$id").is(userId));
        participantQuery.fields().include("conversation");
        List<Document> participants = mongoTemplate.find(
                participantQuery, Document.class, PARTICIPANTS_COLLECTION);

        List<Object> conversationIds = new ArrayList<>();
        for (Document participant : participants) {
            Object conversation = participant.get("conversation");
            if (conversation instanceof Document conversationRef) {
                conversationIds.add(conversationRef.get("$id"));
            }
        }
        return conversationIds;
    }

    private void applyTenantScope(Query query) {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            query.addCriteria(Criteria.where("organization.$id").is(tenantId));
        }
    }
}