package com.talentFlow.course.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
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
@Document(collection = "course_enrollments")
public class CourseEnrollment extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Course course;

    @DBRef
    private User user;

    private EnrollmentStatus status;

    private LocalDateTime enrolledAt;

    private BigDecimal progressPct = BigDecimal.ZERO;

    private LocalDateTime completedAt;

    private LocalDateTime revokedAt;
}