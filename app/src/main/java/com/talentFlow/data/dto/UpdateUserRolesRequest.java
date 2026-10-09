package com.talentFlow.data.dto;

import com.talentFlow.auth.data.enums.RoleName;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRolesRequest(
        @NotNull RoleName role
) {
}
