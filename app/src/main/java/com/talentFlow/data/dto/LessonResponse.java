package com.talentFlow.data.dto;

import java.util.UUID;

public record LessonResponse(
        UUID id,
        String title,
        String lessonType,
        Integer position,
        String contentUrl,
        String contentText,
        boolean completed
) {
}
