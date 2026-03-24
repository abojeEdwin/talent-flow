package com.talentFlow.common.storage.worker;

import com.talentFlow.common.storage.enums.MediaUploadTargetType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MediaUploadQueueService {
    void enqueue(MediaUploadTargetType targetType, UUID targetId, String folder, MultipartFile file);
}
