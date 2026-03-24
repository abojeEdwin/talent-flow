package com.talentFlow.common.storage.worker;

import com.talentFlow.common.storage.service.FileStorageService;
import com.talentFlow.common.storage.data.repository.MediaUploadJobRepository;
import com.talentFlow.common.storage.data.MediaUploadJob;
import com.talentFlow.common.storage.enums.MediaUploadTargetType;
import com.talentFlow.common.storage.enums.UploadStatus;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseMaterial;
import com.talentFlow.course.domain.enums.MaterialUploadStatus;
import com.talentFlow.course.infrastructure.repository.CourseMaterialRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUploadWorker {

    private final MediaUploadJobRepository mediaUploadJobRepository;
    private final FileStorageService fileStorageService;
    private final CourseRepository courseRepository;
    private final CourseMaterialRepository courseMaterialRepository;

    @Scheduled(fixedDelay = 5000)
    public void processPendingUploads() {
        List<MediaUploadJob> jobs = mediaUploadJobRepository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(UploadStatus.PENDING, LocalDateTime.now());
        for (MediaUploadJob job : jobs) {
            processSingleJob(job.getId());
        }
    }

    @Transactional
    public void processSingleJob(java.util.UUID jobId) {
        MediaUploadJob job = mediaUploadJobRepository.findById(jobId).orElse(null);
        if (job == null || job.getStatus() != UploadStatus.PENDING) {
            return;
        }

        try {
            job.setStatus(UploadStatus.PROCESSING);
            job.setAttempts(job.getAttempts() + 1);
            mediaUploadJobRepository.save(job);

            String uploadedUrl = fileStorageService.uploadBytes(
                    job.getPayload(),
                    job.getOriginalFilename(),
                    job.getContentType(),
                    job.getBucketFolder()
            );
            applyUploadedUrl(job.getTargetType(), job.getTargetId(), uploadedUrl);

            job.setStatus(UploadStatus.COMPLETED);
            job.setUploadedUrl(uploadedUrl);
            job.setPayload(null);
            job.setLastError(null);
            mediaUploadJobRepository.save(job);
        } catch (Exception exception) {
            log.warn("Media upload job {} failed on attempt {}: {}", job.getId(), job.getAttempts(), exception.getMessage());
            handleFailure(job, exception.getMessage());
        }
    }

    private void applyUploadedUrl(MediaUploadTargetType targetType, java.util.UUID targetId, String uploadedUrl) {
        switch (targetType) {
            case COURSE_COVER -> {
                Course course = courseRepository.findById(targetId).orElseThrow();
                course.setCoverImageUrl(uploadedUrl);
                courseRepository.save(course);
            }
            case COURSE_INTRO_VIDEO -> {
                Course course = courseRepository.findById(targetId).orElseThrow();
                course.setIntroVideoUrl(uploadedUrl);
                courseRepository.save(course);
            }
            case COURSE_MATERIAL -> {
                CourseMaterial material = courseMaterialRepository.findById(targetId).orElseThrow();
                material.setContentUrl(uploadedUrl);
                material.setUploadStatus(MaterialUploadStatus.COMPLETED);
                courseMaterialRepository.save(material);
            }
        }
    }

    private void handleFailure(MediaUploadJob job, String errorMessage) {
        job.setLastError(errorMessage);
        if (job.getAttempts() >= job.getMaxAttempts()) {
            job.setStatus(UploadStatus.FAILED);
            if (job.getTargetType() == MediaUploadTargetType.COURSE_MATERIAL) {
                courseMaterialRepository.findById(job.getTargetId()).ifPresent(material -> {
                    material.setUploadStatus(MaterialUploadStatus.FAILED);
                    courseMaterialRepository.save(material);
                });
            }
        } else {
            job.setStatus(UploadStatus.PENDING);
            long backoffSeconds = (long) Math.pow(2, Math.max(1, job.getAttempts()));
            job.setNextAttemptAt(LocalDateTime.now().plusSeconds(backoffSeconds));
        }
        mediaUploadJobRepository.save(job);
    }
}
