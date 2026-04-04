package com.talentFlow.auth.infrastructure.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnMissingBean(AuthMailService.class)
public class NoopAuthMailServiceImpl implements AuthMailService {

    @Override
    public void sendVerificationEmail(String recipientEmail, String recipientName, String verificationLink) {
        log.warn("Mail sender unavailable. Skipping verification email for {}", recipientEmail);
    }

    @Override
    public void sendInstructorWelcomeEmail(String recipientEmail, String recipientName, String temporaryPassword, String loginUrl) {
        log.warn("Mail sender unavailable. Skipping instructor welcome email for {}", recipientEmail);
    }

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink) {
        log.warn("Mail sender unavailable. Skipping password reset email for {}", recipientEmail);
    }
}
