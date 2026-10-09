package com.talentFlow.data.entity;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.Enums.ParticipantRole;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.common.service.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "chat_conversation_participants")
public class ConversationParticipant extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Conversation conversation;

    @DBRef
    private User user;

    private ParticipantRole role;

    public LocalDateTime getJoinedAt() {
        return getCreatedAt();
    }
}