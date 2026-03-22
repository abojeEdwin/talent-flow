package com.talentFlow.admin.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CohortResponse(
        UUID id,
        String name,
        String description,
        Integer intakeYear,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive
) {
}
