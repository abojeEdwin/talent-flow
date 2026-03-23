package com.talentFlow.course.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CourseDetailResponse(
        UUID id,
        String title,
        String description,
        String status,
        BigDecimal progressPct,
        List<CourseModuleResponse> modules
) {
}
