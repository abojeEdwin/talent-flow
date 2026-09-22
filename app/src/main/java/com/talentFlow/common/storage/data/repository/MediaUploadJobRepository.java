package com.talentFlow.common.storage.data.repository;

import com.talentFlow.common.storage.data.MediaUploadJob;
import com.talentFlow.common.storage.enums.UploadStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MediaUploadJobRepository extends MongoRepository<MediaUploadJob, UUID> {
    List<MediaUploadJob> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            UploadStatus status,
            LocalDateTime nextAttemptAt
    );
}