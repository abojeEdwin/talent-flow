package com.talentFlow.common.storage.worker;

import com.talentFlow.common.exception.ApiException;
import com.talentFlow.common.storage.data.repository.MediaUploadJobRepository;
import com.talentFlow.common.storage.data.MediaUploadJob;
import com.talentFlow.common.storage.enums.MediaUploadTargetType;
import com.talentFlow.common.storage.enums.UploadStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaUploadQueueServiceImpl implements MediaUploadQueueService {
    private final MediaUploadJobRepository mediaUploadJobRepository;

    @Override
    public void enqueue(MediaUploadTargetType targetType, UUID targetId, String folder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Upload file is required");
        }
        MediaUploadJob job = new MediaUploadJob();
        job.setTargetType(targetType);
        job.setTargetId(targetId);
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

        mediaUploadJobRepository.save(job);
    }
}
