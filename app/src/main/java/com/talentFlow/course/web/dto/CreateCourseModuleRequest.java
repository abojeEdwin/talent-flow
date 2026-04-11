package com.talentFlow.course.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCourseModuleRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 180, message = "Title must be at most 180 characters")
        String title,

        @NotNull(message = "Position is required")
        Integer position
) {
}
