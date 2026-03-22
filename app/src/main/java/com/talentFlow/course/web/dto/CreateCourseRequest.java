package com.talentFlow.course.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 5000) String description
) {
}
