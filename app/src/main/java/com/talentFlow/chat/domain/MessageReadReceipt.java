package com.talentFlow.chat.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "chat_message_read_receipts")
public class MessageReadReceipt extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Message message;

    @DBRef
    private User user;

    private LocalDateTime readAt;
}