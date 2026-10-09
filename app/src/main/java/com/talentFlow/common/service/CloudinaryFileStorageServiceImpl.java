package com.talentFlow.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.talentFlow.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryFileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }
        try {
            return upload(file.getBytes(), file.getOriginalFilename(), file.getContentType(), folder);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read upload file");
        }
    }

    @Override
    public String uploadBytes(byte[] payload, String originalFilename, String contentType, String folder) {
        if (payload == null || payload.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Upload payload is empty");
        }
        return upload(payload, originalFilename, contentType, folder);
    }

    private String upload(byte[] payload, String originalFilename, String contentType, String folder) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", normalizeFolder(folder),
                    "public_id", UUID.randomUUID().toString(),
                    "resource_type", resourceType(contentType, originalFilename)
            );
            if (contentType != null && !contentType.isBlank()) {
                options.put("context", "content_type=" + contentType);
            }
            Map<?, ?> result = cloudinary.uploader().upload(payload, options);
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new IllegalStateException("Cloudinary did not return a secure URL");
            }
            return secureUrl.toString();
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file to Cloudinary");
        }
    }

    private String resourceType(String contentType, String filename) {
        String normalizedType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (normalizedType.startsWith("image/")) {
            return "image";
        }
        if (normalizedType.startsWith("video/")) {
            return "video";
        }
        String normalizedName = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (normalizedName.matches(".*\\.(png|jpe?g|gif|webp|svg)$")) {
            return "image";
        }
        if (normalizedName.matches(".*\\.(mp4|mov|avi|webm|mkv)$")) {
            return "video";
        }
        return "raw";
    }

    private String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "uploads";
        }
        return folder.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
