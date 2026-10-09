package com.talentFlow.auth.data.entity;

import com.talentFlow.auth.data.enums.RoleName;
import com.talentFlow.auth.data.enums.UserStatus;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.data.entity.Organization;
import com.talentFlow.common.service.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "users")
public class User extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private String firstName;

    private String lastName;

    private RoleName role;

    private UserStatus status;

    private int failedLoginAttempts;

    private LocalDateTime lockedUntil;

    private LocalDateTime lastLoginAt;
}