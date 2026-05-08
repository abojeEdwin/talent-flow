package com.talentFlow.media.application;

import com.talentFlow.common.exception.ApiException;
import com.talentFlow.common.storage.config.S3Properties;
import com.talentFlow.common.storage.service.FileStorageService;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaUrlServiceImpl implements MediaUrlService {

    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;
    private final S3Properties s3Properties;

    @Override
    public String toAccessibleMediaUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return rawUrl;
        }

        String objectKey = extractS3ObjectKey(rawUrl);
        if (objectKey == null) {
            return rawUrl;
        }

        try {
            return fileStorageService.generatePresignedUrl(objectKey);
        } catch (Exception exception) {
            log.warn("Failed to generate presigned URL for object key {}", objectKey, exception);
            return rawUrl;
        }
    }

    @Override
    public String getCourseCoverImagePresignedUrl(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));

        String coverImageUrl = course.getCoverImageUrl();
        if (coverImageUrl == null || coverImageUrl.isBlank()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Course has no cover image");
        }

        String objectKey = extractS3ObjectKey(coverImageUrl);
        if (objectKey == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Course cover is not stored in configured S3 bucket");
        }
        return fileStorageService.generatePresignedUrl(objectKey);
    }

    private String extractS3ObjectKey(String rawUrl) {
        String bucketName = s3Properties.getS3BucketName();
        if (bucketName == null || bucketName.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(rawUrl);
            String host = uri.getHost();
            String path = uri.getPath();

            if (host == null || host.isBlank() || path == null || path.isBlank()) {
                return null;
            }

            String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
            String lowerHost = host.toLowerCase();
            String lowerBucketName = bucketName.toLowerCase();

            if (lowerHost.startsWith(lowerBucketName + ".") && lowerHost.contains("amazonaws.com")) {
                return normalizedPath;
            }

            String bucketPrefix = bucketName + "/";
            if (lowerHost.contains("amazonaws.com") && normalizedPath.startsWith(bucketPrefix)) {
                return normalizedPath.substring(bucketPrefix.length());
            }

            return null;
        } catch (Exception exception) {
            return null;
        }
    }
}
