package com.talentFlow.admin.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import com.talentFlow.tenant.listener.TenantEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "admin_audit_logs")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@EntityListeners(TenantEntityListener.class)
public class AdminAuditLog extends BaseEntity implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actorUser;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 100)
    private String resourceType;

    private UUID resourceId;

    @Column(columnDefinition = "TEXT")
    private String details;
}
