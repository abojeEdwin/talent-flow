package com.talentFlow.course.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "courses")
public class Course extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    private String title;

    private String description;

    private String coverImageUrl;

    private String introVideoUrl;

    private CourseStatus status;

    @DBRef
    private User createdByUser;

    private LocalDateTime publishedAt;

    private LocalDateTime archivedAt;
}