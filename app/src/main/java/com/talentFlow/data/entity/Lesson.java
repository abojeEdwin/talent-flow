package com.talentFlow.data.entity;

import com.talentFlow.data.BaseEntity;
import com.talentFlow.data.Enums.LessonType;
import com.talentFlow.data.Enums.LessonUploadStatus;
import com.talentFlow.common.service.TenantAware;
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