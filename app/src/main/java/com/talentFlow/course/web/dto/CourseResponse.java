package com.talentFlow.course.web.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String title,
        String description,
        String coverImageUrl,
        String introVideoUrl,
        String status,
        LocalDateTime publishedAt,
        LocalDateTime archivedAt,
        UUID createdByUserId,
        Set<UUID> instructorIds
) {
}
