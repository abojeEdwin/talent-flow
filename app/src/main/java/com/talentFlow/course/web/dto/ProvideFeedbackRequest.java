package com.talentFlow.course.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ProvideFeedbackRequest(
        @NotBlank String comment,
        BigDecimal score
) {
}
