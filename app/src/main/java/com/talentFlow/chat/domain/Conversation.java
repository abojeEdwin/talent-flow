package com.talentFlow.chat.domain;

import com.talentFlow.admin.domain.Cohort;
import com.talentFlow.admin.domain.ProjectTeam;
import com.talentFlow.auth.domain.User;
import com.talentFlow.chat.domain.enums.ChatType;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
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