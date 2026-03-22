package com.talentFlow.course.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateAssignmentRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 5000) String instructions,
        LocalDateTime dueAt,
        BigDecimal maxScore
) {
}
