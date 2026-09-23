package com.talentFlow.chat.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.chat.domain.enums.MessageType;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "chat_messages")
public class Message extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Conversation conversation;

    @DBRef
    private User sender;

    private String content;

    private MessageType messageType = MessageType.TEXT;

    @DBRef
    private Message replyToMessage;
}