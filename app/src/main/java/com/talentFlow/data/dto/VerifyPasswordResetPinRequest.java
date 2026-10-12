package com.talentFlow.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPasswordResetPinRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "PIN must be exactly 6 digits")
        String pin
) {
}
