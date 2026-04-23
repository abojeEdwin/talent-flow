package com.talentFlow.chat.web.dto;

import com.talentFlow.chat.domain.enums.ChatType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {
    @NotNull(message = "Chat type is required")
    private ChatType type;

    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    private UUID cohortId;

    private UUID teamId;
}