package com.talentFlow.course.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InstructorProgressResponse(
        UUID courseId,
        String courseTitle,
        UUID learnerId,
        String learnerEmail,
        String learnerName,
        String enrollmentStatus,
        BigDecimal progressPct,
        int totalAssignments,
        int submittedAssignments,
        BigDecimal averageScore
) {
}
