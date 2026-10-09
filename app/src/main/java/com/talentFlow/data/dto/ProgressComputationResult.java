package com.talentFlow.data.dto;

import com.talentFlow.data.Enums.EnrollmentStatus;

import java.math.BigDecimal;

public record ProgressComputationResult(
        BigDecimal progressPct,
        EnrollmentStatus enrollmentStatus,
        boolean certificateQueued
) {
}
