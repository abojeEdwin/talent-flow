package com.talentFlow.auth.data.entity;

import com.talentFlow.data.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "password_reset_tokens")
public class PasswordResetToken extends BaseEntity {

    @DBRef
    private User user;

    @Indexed(unique = true)
    private String token;

    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;
}