package com.talentFlow.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateProjectTeamRequest(
        @NotNull UUID cohortId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String description
) {
}
