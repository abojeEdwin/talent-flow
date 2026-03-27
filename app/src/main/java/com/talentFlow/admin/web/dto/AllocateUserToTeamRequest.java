package com.talentFlow.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AllocateUserToTeamRequest(
        @NotNull UUID userId,
        @NotBlank @Size(max = 100) String teamRole
) {
}
