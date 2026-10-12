package com.talentFlow.data.dto;

public record PasswordResetVerificationResponse(
        String resetToken,
        String message
) {
}
