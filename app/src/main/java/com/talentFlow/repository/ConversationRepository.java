package com.talentFlow.repository;

import com.talentFlow.data.entity.Conversation;
import com.talentFlow.data.Enums.ChatType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends MongoRepository<Conversation, UUID>, ConversationRepositoryCustom {

    default Optional<Conversation> findByCohortId(UUID cohortId) {
        return findByTypeAndCohortId(ChatType.COHORT_CHAT, cohortId);
    }

    default Optional<Conversation> findByTeamId(UUID teamId) {
        return findByTypeAndTeamId(ChatType.TEAM_CHAT, teamId);
    }

    Optional<Conversation> findByTypeAndCohortId(ChatType type, UUID cohortId);

    Optional<Conversation> findByTypeAndTeamId(ChatType type, UUID teamId);
}