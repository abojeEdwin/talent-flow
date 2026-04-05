package com.talentFlow.auth.infrastructure.mail;

import com.talentFlow.auth.infrastructure.mail.queue.EmailJobStatus;
import com.talentFlow.auth.infrastructure.mail.queue.EmailJobType;
import com.talentFlow.auth.infrastructure.mail.queue.OutboundEmailJob;
import com.talentFlow.auth.infrastructure.mail.queue.OutboundEmailJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthMailServiceImpl implements AuthMailService {

    private final OutboundEmailJobRepository outboundEmailJobRepository;

    @Override
    public void sendVerificationEmail(String recipientEmail, String recipientName, String verificationLink) {
        enqueue(EmailJobType.VERIFICATION, recipientEmail, recipientName, verificationLink, null, null);
    }

    @Override
    public void sendInstructorWelcomeEmail(String recipientEmail, String recipientName, String temporaryPassword, String loginUrl) {
        enqueue(EmailJobType.INSTRUCTOR_WELCOME, recipientEmail, recipientName, null, temporaryPassword, loginUrl);
    }

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink) {
        enqueue(EmailJobType.PASSWORD_RESET, recipientEmail, recipientName, resetLink, null, null);
    }

    private void enqueue(
            EmailJobType type,
            String recipientEmail,
            String recipientName,
            String link,
            String temporaryPassword,
            String loginUrl
    ) {
        OutboundEmailJob job = new OutboundEmailJob();
        job.setType(type);
        job.setRecipientEmail(recipientEmail);
        job.setRecipientName(recipientName);
        job.setLink(link);
        job.setTemporaryPassword(temporaryPassword);
        job.setLoginUrl(loginUrl);
        job.setStatus(EmailJobStatus.PENDING);
        job.setAttempts(0);
        job.setMaxAttempts(5);
        job.setNextAttemptAt(LocalDateTime.now());
        outboundEmailJobRepository.save(job);
        log.info("Queued {} email for {}", type, recipientEmail);
    }
}
