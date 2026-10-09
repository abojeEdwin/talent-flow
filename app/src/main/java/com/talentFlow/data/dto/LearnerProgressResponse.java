package com.talentFlow.data.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LearnerProgressResponse(
        UUID learnerId,
        String learnerEmail,
        String learnerName,
        int totalAssignments,
        int submittedAssignments,
        BigDecimal averageScore
) {
}
