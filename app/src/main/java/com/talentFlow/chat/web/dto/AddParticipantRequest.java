package com.talentFlow.chat.web.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddParticipantRequest {
    @NotEmpty(message = "At least one user ID is required")
    private List<UUID> userIds;
}