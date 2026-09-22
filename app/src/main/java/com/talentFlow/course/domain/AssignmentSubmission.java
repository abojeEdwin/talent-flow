package com.talentFlow.course.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.course.domain.enums.SubmissionStatus;
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
@Document(collection = "assignment_submissions")
public class AssignmentSubmission extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Assignment assignment;

    @DBRef
    private User learnerUser;

    private String contentUrl;

    private LocalDateTime submittedAt;

    private BigDecimal score;

    private SubmissionStatus status;
}