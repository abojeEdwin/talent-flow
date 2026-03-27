package com.talentFlow.admin.web.dto;

import com.talentFlow.auth.domain.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull UserStatus status
) {
}
