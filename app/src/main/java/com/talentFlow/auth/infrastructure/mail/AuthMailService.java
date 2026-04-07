package com.talentFlow.auth.infrastructure.mail;

public interface AuthMailService {
    void sendInstructorWelcomeEmail(String recipientEmail, String recipientName, String temporaryPassword, String loginUrl);

    void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink);
}
