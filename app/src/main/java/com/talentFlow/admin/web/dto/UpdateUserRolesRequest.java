package com.talentFlow.admin.web.dto;

import com.talentFlow.auth.domain.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty Set<@NotNull RoleName> roles
) {
}
