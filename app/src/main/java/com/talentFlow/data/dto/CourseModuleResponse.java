package com.talentFlow.data.dto;

import java.util.List;
import java.util.UUID;

public record CourseModuleResponse(
        UUID id,
        String title,
        Integer position,
        List<LessonResponse> lessons
) {
}
