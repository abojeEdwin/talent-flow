package com.talentFlow.course.web.dto;

import java.util.UUID;

public record CourseMaterialResponse(
        UUID id,
        UUID courseId,
        String title,
        String materialType,
        String contentUrl,
        UUID uploadedByUserId
) {
}
