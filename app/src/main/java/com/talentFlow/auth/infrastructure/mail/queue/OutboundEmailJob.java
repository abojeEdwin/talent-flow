package com.talentFlow.auth.infrastructure.mail.queue;

import com.talentFlow.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "outbound_email_jobs")
public class OutboundEmailJob extends BaseEntity {

    private EmailJobType type;

    private String recipientEmail;

    private String recipientName;

    private String link;

    private String temporaryPassword;

    private String loginUrl;

    private EmailJobStatus status;

    private Integer attempts;

    private Integer maxAttempts;

    private LocalDateTime nextAttemptAt;

    private String lastError;
}