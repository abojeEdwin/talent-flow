package com.talentFlow.data.dto;

import java.util.UUID;

public record OnboardLearnerResponse(
        UUID userId,
        String email,
        String message
) {
}