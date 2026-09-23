package com.talentFlow.course.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "lesson_progress")
public class LessonProgress extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private User user;

    @DBRef
    private Lesson lesson;

    private boolean completed;

    private LocalDateTime completedAt;
}