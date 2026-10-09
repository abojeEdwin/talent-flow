package com.talentFlow.data.entity;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.common.service.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "course_instructors")
public class CourseInstructor extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Course course;

    @DBRef
    private User instructorUser;

    private boolean isPrimary;
}