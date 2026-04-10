package com.talentFlow.common.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String folder);
    String uploadBytes(byte[] payload, String originalFilename, String contentType, String folder);
    String generatePresignedUrl(String objectKey);
}
