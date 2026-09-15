package com.talentFlow.chat.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.chat.domain.enums.MessageType;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import com.talentFlow.tenant.listener.TenantEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chat_messages")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@EntityListeners(TenantEntityListener.class)
public class Message extends BaseEntity implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType = MessageType.TEXT;

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private List<MessageReadReceipt> readReceipts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;
}