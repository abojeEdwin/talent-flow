package com.talentFlow.admin.web.dto;

import com.talentFlow.auth.domain.enums.RoleName;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRolesRequest(
        @NotNull RoleName role
) {
}
