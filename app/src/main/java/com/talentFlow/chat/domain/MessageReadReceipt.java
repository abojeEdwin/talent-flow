package com.talentFlow.chat.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import com.talentFlow.tenant.listener.TenantEntityListener;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chat_message_read_receipts")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@EntityListeners(TenantEntityListener.class)
public class MessageReadReceipt extends BaseEntity implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime readAt;
}