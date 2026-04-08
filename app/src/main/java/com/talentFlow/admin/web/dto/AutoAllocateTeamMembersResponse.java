package com.talentFlow.admin.web.dto;

import java.util.List;
import java.util.UUID;

public record AutoAllocateTeamMembersResponse(
        UUID teamId,
        int allocatedCount,
        int maxTeamSize,
        List<TeamMemberResponse> members
) {
}
