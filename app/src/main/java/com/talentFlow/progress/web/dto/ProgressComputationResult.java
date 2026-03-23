package com.talentFlow.progress.web.dto;

import com.talentFlow.course.domain.enums.EnrollmentStatus;

import java.math.BigDecimal;

public record ProgressComputationResult(
        BigDecimal progressPct,
        EnrollmentStatus enrollmentStatus,
        boolean certificateQueued
) {
}
