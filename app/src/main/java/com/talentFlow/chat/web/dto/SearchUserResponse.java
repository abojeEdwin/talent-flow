package com.talentFlow.chat.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchUserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}