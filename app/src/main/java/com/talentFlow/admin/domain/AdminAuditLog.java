package com.talentFlow.admin.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Document(collection = "admin_audit_logs")
public class AdminAuditLog extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private User actorUser;

    private String action;

    private String resourceType;

    private UUID resourceId;

    private String details;
}