package com.talentFlow.data.dto;

import java.util.UUID;

public record AssignmentFeedbackResponse(
        UUID id,
        UUID submissionId,
        UUID instructorUserId,
        String comment
) {
}
