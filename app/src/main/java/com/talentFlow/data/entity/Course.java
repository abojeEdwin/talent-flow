package com.talentFlow.data.entity;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.data.Enums.CourseStatus;
import com.talentFlow.common.service.TenantAware;
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