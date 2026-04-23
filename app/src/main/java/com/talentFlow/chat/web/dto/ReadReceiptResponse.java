package com.talentFlow.chat.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptResponse {
    private List<ReceiptDetail> receipts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptDetail {
        private UUID messageId;
        private UUID userId;
        private String firstName;
        private String lastName;
        private LocalDateTime readAt;
    }
}