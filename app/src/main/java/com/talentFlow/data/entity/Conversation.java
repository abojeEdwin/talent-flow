package com.talentFlow.data.entity;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.Enums.ChatType;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.common.service.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "chat_conversations")
public class Conversation extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    private ChatType type;

    private String name;

    @DBRef
    private Cohort cohort;

    @DBRef
    private ProjectTeam team;

    @DBRef
    private User createdBy;
}