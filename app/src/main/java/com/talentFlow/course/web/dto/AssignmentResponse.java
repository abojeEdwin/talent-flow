package com.talentFlow.course.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        UUID courseId,
        String title,
        String instructions,
        LocalDateTime dueAt,
        BigDecimal maxScore,
        UUID createdByUserId
) {
}
