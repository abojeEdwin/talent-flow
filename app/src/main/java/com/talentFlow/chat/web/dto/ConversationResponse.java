package com.talentFlow.chat.web.dto;

import com.talentFlow.chat.domain.enums.ChatType;
import com.talentFlow.chat.domain.enums.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private UUID id;
    private ChatType type;
    private String name;
    private UUID cohortId;
    private String cohortName;
    private UUID teamId;
    private String teamName;
    private List<ParticipantResponse> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer unreadCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantResponse {
        private UUID userId;
        private String firstName;
        private String lastName;
        private String email;
        private ParticipantRole role;
        private LocalDateTime joinedAt;
    }
}