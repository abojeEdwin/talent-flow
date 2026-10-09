package com.talentFlow.data.entity;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.common.service.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "notifications")
public class Notification extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private User user;

    private String type;

    private String title;

    private String message;

    private String payload;

    private boolean read;

    private LocalDateTime readAt;
}