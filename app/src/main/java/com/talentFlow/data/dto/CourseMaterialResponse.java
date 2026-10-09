package com.talentFlow.data.dto;

import java.util.UUID;

public record CourseMaterialResponse(
        UUID id,
        UUID courseId,
        String title,
        String materialType,
        String contentUrl,
        String uploadStatus,
        UUID uploadedByUserId
) {
}
