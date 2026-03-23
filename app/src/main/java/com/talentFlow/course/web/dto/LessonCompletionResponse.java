package com.talentFlow.course.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LessonCompletionResponse(
        UUID lessonId,
        UUID courseId,
        BigDecimal progressPct,
        String enrollmentStatus,
        boolean certificateQueued
) {
}
