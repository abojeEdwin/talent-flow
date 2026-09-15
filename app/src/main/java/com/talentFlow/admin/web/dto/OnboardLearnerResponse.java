package com.talentFlow.admin.web.dto;

import java.util.UUID;

public record OnboardLearnerResponse(
        UUID userId,
        String email,
        String message
) {
}