package com.talentFlow.data.dto;

import com.talentFlow.data.Enums.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLessonRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 180, message = "Title must be at most 180 characters")
        String title,

        @NotNull(message = "Lesson type is required")
        LessonType lessonType,

        @NotNull(message = "Position is required")
        Integer position,

        String contentUrl,

        String contentText
) {
}
