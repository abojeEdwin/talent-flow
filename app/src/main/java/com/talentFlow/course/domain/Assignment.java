package com.talentFlow.course.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "assignments")
public class Assignment extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Course course;

    private String title;

    private String instructions;

    private LocalDateTime dueAt;

    private BigDecimal maxScore;

    @DBRef
    private User createdByUser;
}