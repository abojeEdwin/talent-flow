package com.talentFlow.auth.infrastructure.mail.queue;

import com.talentFlow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "outbound_email_jobs")
public class OutboundEmailJob extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmailJobType type;

    @Column(nullable = false, length = 255)
    private String recipientEmail;

    @Column(nullable = false, length = 120)
    private String recipientName;

    @Column(length = 500)
    private String link;

    @Column(length = 200)
    private String temporaryPassword;

    @Column(length = 500)
    private String loginUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailJobStatus status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Integer maxAttempts;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;
}
