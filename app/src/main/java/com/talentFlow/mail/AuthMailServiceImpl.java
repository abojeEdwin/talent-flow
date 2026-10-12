package com.talentFlow.mail;

import com.talentFlow.mail.queue.EmailJobStatus;
import com.talentFlow.mail.queue.EmailJobType;
import com.talentFlow.mail.queue.OutboundEmailJob;
import com.talentFlow.mail.queue.OutboundEmailJobRepository;
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
    public void sendInstructorWelcomeEmail(String recipientEmail, String recipientName, String temporaryPassword, String loginUrl) {
        enqueue(EmailJobType.INSTRUCTOR_WELCOME, recipientEmail, recipientName, null, temporaryPassword, loginUrl);
    }

    @Override
    public void sendLearnerWelcomeEmail(String recipientEmail, String recipientName, String temporaryPassword, String loginUrl) {
        enqueue(EmailJobType.LEARNER_WELCOME, recipientEmail, recipientName, null, temporaryPassword, loginUrl);
    }

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink) {
        enqueue(EmailJobType.PASSWORD_RESET, recipientEmail, recipientName, resetLink, null, null);
    }

    @Override
    public void sendPasswordResetPinEmail(String recipientEmail, String recipientName, String pin) {
        enqueue(EmailJobType.PASSWORD_RESET, recipientEmail, recipientName, null, null, null, pin);
    }

    private void enqueue(
            EmailJobType type,
            String recipientEmail,
            String recipientName,
            String link,
            String temporaryPassword,
            String loginUrl
    ) {
        enqueue(type, recipientEmail, recipientName, link, temporaryPassword, loginUrl, null);
    }

    private void enqueue(
            EmailJobType type,
            String recipientEmail,
            String recipientName,
            String link,
            String temporaryPassword,
            String loginUrl,
            String pin
    ) {
        OutboundEmailJob job = new OutboundEmailJob();
        job.setType(type);
        job.setRecipientEmail(recipientEmail);
        job.setRecipientName(recipientName);
        job.setLink(link);
        job.setTemporaryPassword(temporaryPassword);
        job.setLoginUrl(loginUrl);
        job.setPin(pin);
        job.setStatus(EmailJobStatus.PENDING);
        job.setAttempts(0);
        job.setMaxAttempts(5);
        job.setNextAttemptAt(LocalDateTime.now());
        outboundEmailJobRepository.save(job);
        log.info("Queued {} email for {}", type, recipientEmail);
    }
}
