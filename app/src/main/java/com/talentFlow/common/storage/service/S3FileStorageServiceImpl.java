package com.talentFlow.common.storage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.common.storage.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileStorageServiceImpl implements FileStorageService {

    private final AmazonS3 amazonS3;
    private final S3Properties s3Properties;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        String key = buildObjectKey(folder, file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (file.getContentType() != null) {
            metadata.setContentType(file.getContentType());
        }

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(s3Properties.getS3BucketName(), key, inputStream, metadata);
            return amazonS3.getUrl(s3Properties.getS3BucketName(), key).toString();
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read upload file");
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file to S3");
        }
    }

    @Override
    public String uploadBytes(byte[] payload, String originalFilename, String contentType, String folder) {
        if (payload == null || payload.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Upload payload is empty");
        }

        String key = buildObjectKey(folder, originalFilename);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(payload.length);
        if (contentType != null && !contentType.isBlank()) {
            metadata.setContentType(contentType);
        }

        try (InputStream inputStream = new ByteArrayInputStream(payload)) {
            amazonS3.putObject(s3Properties.getS3BucketName(), key, inputStream, metadata);
            return amazonS3.getUrl(s3Properties.getS3BucketName(), key).toString();
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file to S3");
        }
    }

    private String buildObjectKey(String folder, String originalFilename) {
        String safeFolder = folder == null ? "uploads" : folder.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        String name = originalFilename == null || originalFilename.isBlank() ? "file" : originalFilename;
        String sanitized = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
        return safeFolder + "/" + UUID.randomUUID() + "-" + sanitized;
    }
}
