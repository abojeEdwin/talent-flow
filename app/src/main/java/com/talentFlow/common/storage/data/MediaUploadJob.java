package com.talentFlow.common.storage.data;

import com.talentFlow.common.BaseEntity;
import com.talentFlow.common.storage.enums.MediaUploadTargetType;
import com.talentFlow.common.storage.enums.UploadStatus;
import com.talentFlow.organization.domain.Organization;
import com.talentFlow.tenant.TenantAware;
import com.talentFlow.tenant.listener.TenantEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "media_upload_jobs")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@EntityListeners(TenantEntityListener.class)
public class MediaUploadJob extends BaseEntity implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MediaUploadTargetType targetType;

    @Column(nullable = false)
    private UUID targetId;

    private UUID initiatedByUserId;

    @Column(nullable = false, length = 120)
    private String bucketFolder;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(length = 120)
    private String contentType;
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "bytea")
    private byte[] payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UploadStatus status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Integer maxAttempts;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(length = 500)
    private String uploadedUrl;

    @Column(columnDefinition = "TEXT")
    private String lastError;
}
