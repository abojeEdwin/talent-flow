package com.talentFlow.course.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "assignment_feedback")
public class AssignmentFeedback extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private AssignmentSubmission submission;

    @DBRef
    private User instructorUser;

    private String comment;
}