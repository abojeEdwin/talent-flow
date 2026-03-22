package com.talentFlow.admin.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCohortRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String description,
        @NotNull @Min(2020) @Max(2100) Integer intakeYear,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
