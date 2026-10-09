package com.talentFlow.data.entity;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.Enums.MessageType;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.common.service.TenantAware;
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