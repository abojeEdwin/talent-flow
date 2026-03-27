package com.talentFlow.course.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record AssignInstructorsRequest(
        @NotNull UUID primaryInstructorId,
        Set<UUID> coInstructorIds
) {
}
