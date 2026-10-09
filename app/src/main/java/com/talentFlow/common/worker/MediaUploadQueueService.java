package com.talentFlow.common.worker;

import com.talentFlow.common.data.enums.MediaUploadTargetType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MediaUploadQueueService {
    void enqueue(MediaUploadTargetType targetType, UUID targetId, UUID initiatedByUserId, String folder, MultipartFile file);
}
