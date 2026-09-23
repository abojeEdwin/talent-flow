package com.talentFlow.course.domain;

import com.talentFlow.common.BaseEntity;
import com.talentFlow.course.domain.enums.LessonType;
import com.talentFlow.course.domain.enums.LessonUploadStatus;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "lessons")
public class Lesson extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private CourseModule module;

    private String title;

    private LessonType lessonType;

    private String contentUrl;

    private String contentText;

    private Integer position;

    private LessonUploadStatus uploadStatus = LessonUploadStatus.COMPLETED;
}