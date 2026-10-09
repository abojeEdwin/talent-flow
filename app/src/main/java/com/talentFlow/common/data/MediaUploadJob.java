package com.talentFlow.common.data;

import com.talentFlow.common.data.enums.MediaUploadTargetType;
import com.talentFlow.common.data.enums.UploadStatus;
import com.talentFlow.data.BaseEntity;
import com.talentFlow.data.entity.Organization;
import com.talentFlow.common.service.TenantAware;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "media_upload_jobs")
public class MediaUploadJob extends BaseEntity implements TenantAware {

    @DBRef
    private Organization organization;

    private MediaUploadTargetType targetType;

    private UUID targetId;

    private UUID initiatedByUserId;

    private String folder;

    private String originalFilename;

    private String contentType;

    private byte[] payload;

    private UploadStatus status;

    private Integer attempts;

    private Integer maxAttempts;

    private LocalDateTime nextAttemptAt;

    private String uploadedUrl;

    private String lastError;
}