package com.talentFlow.chat.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private UUID id;
    private String content;
    private SenderResponse sender;
    private UUID replyToMessageId;
    private String replyToContent;
    private Boolean isRead;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SenderResponse {
        private UUID id;
        private String firstName;
        private String lastName;
    }
}