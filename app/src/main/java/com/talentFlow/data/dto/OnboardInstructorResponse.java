package com.talentFlow.data.dto;

import java.util.UUID;

public record OnboardInstructorResponse(
        UUID userId,
        String email,
        String message
) {
}
