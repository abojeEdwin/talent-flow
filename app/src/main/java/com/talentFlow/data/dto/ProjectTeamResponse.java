package com.talentFlow.data.dto;

import java.util.UUID;

public record ProjectTeamResponse(
        UUID id,
        UUID cohortId,
        String name,
        String description
) {
}
