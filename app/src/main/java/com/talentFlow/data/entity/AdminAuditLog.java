package com.talentFlow.data.entity;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.common.service.TenantAware;
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