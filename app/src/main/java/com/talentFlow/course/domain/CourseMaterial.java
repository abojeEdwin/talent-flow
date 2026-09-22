package com.talentFlow.course.domain;

import com.talentFlow.auth.domain.User;
import com.talentFlow.common.BaseEntity;
import com.talentFlow.course.domain.enums.MaterialType;
import com.talentFlow.course.domain.enums.MaterialUploadStatus;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "course_materials")
public class CourseMaterial extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    @DBRef
    private Course course;

    private String title;

    private MaterialType materialType;

    private String contentUrl;

    private MaterialUploadStatus uploadStatus = MaterialUploadStatus.COMPLETED;

    @DBRef
    private User uploadedByUser;
}