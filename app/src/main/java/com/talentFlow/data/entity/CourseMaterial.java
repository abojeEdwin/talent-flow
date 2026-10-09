package com.talentFlow.data.entity;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.data.Enums.MaterialType;
import com.talentFlow.data.Enums.MaterialUploadStatus;
import com.talentFlow.common.service.TenantAware;
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