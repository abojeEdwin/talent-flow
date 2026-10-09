package com.talentFlow.data.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseProgressUpdateMessage(
        UUID userId,
        UUID courseId,
        BigDecimal progressPct,
        String enrollmentStatus
) {
}
