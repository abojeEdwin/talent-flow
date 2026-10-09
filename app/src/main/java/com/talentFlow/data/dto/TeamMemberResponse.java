package com.talentFlow.data.dto;

import java.util.UUID;

public record TeamMemberResponse(
        UUID userId,
        String email,
        String fullName,
        String teamRole
) {
}
