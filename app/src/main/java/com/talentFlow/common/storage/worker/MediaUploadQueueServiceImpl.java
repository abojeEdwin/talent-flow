package com.talentFlow.common.storage.worker;

import com.talentFlow.common.exception.ApiException;
import com.talentFlow.common.storage.data.repository.MediaUploadJobRepository;
import com.talentFlow.common.storage.data.MediaUploadJob;
import com.talentFlow.common.storage.enums.MediaUploadTargetType;
import com.talentFlow.common.storage.enums.UploadStatus;
import com.talentFlow.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaUploadQueueServiceImpl implements MediaUploadQueueService {
    private final MediaUploadJobRepository mediaUploadJobRepository;
    private final NotificationService notificationService;

    @Override
    public void enqueue(MediaUploadTargetType targetType,
                        UUID targetId,
                        UUID initiatedByUserId,
                        String folder,
                        MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Upload file is required");
        }
        MediaUploadJob job = new MediaUploadJob();
        job.setTargetType(targetType);
        job.setTargetId(targetId);
        job.setInitiatedByUserId(initiatedByUserId);
        job.setBucketFolder(folder);
        job.setOriginalFilename(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        job.setContentType(file.getContentType());
        job.setStatus(UploadStatus.PENDING);
        job.setAttempts(0);
        job.setMaxAttempts(5);
        job.setNextAttemptAt(LocalDateTime.now());
        try {
            job.setPayload(file.getBytes());
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unable to read upload file");
        }

        MediaUploadJob saved = mediaUploadJobRepository.save(job);
        publishUploadNotification(saved, "Upload queued", "Your upload is pending processing");
    }

    private void publishUploadNotification(MediaUploadJob job, String title, String message) {
        if (job.getInitiatedByUserId() == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("jobId", job.getId());
        payload.put("targetType", job.getTargetType().name());
        payload.put("targetId", job.getTargetId());
        payload.put("status", job.getStatus().name());
        payload.put("filename", job.getOriginalFilename());

        notificationService.notifyUser(
                job.getInitiatedByUserId(),
                "UPLOAD_STATUS",
                title,
                message,
                payload
        );
    }
}
