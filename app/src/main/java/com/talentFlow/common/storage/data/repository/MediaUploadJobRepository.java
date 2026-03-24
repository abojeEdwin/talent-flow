package com.talentFlow.common.storage.data.repository;

import com.talentFlow.common.storage.data.MediaUploadJob;
import com.talentFlow.common.storage.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MediaUploadJobRepository extends JpaRepository<MediaUploadJob, UUID> {
    List<MediaUploadJob> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            UploadStatus status,
            LocalDateTime nextAttemptAt
    );
}
