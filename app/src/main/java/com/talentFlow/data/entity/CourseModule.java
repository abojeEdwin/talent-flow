package com.talentFlow.data.entity;

import com.talentFlow.data.BaseEntity;
import com.talentFlow.common.service.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "course_modules")
public class CourseModule extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Course course;

    private String title;

    private Integer position;
}