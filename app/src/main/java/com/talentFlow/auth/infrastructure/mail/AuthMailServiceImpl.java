package com.talentFlow.auth.infrastructure.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthMailServiceImpl implements AuthMailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Override
    public void sendVerificationEmail(String recipientEmail, String recipientName, String verificationLink) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("Verify your Talent Flow account");
            helper.setText(buildVerificationBody(recipientName, verificationLink), false);

            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send verification email", ex);
        }
    }

    @Override
    public void sendInstructorWelcomeEmail(String recipientEmail, String recipientName, String temporaryPassword, String loginUrl) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("Welcome to Talent Flow - Instructor Onboarding");
            helper.setText(buildInstructorWelcomeBody(recipientName, temporaryPassword, loginUrl), false);

            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send instructor welcome email", ex);
        }
    }

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("Talent Flow password reset");
            helper.setText(buildPasswordResetBody(recipientName, resetLink), false);

            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send password reset email", ex);
        }
    }

    private String buildVerificationBody(String recipientName, String verificationLink) {
        return "Hi " + recipientName + ",\n\n"
                + "Welcome to Talent Flow.\n"
                + "Please verify your account using the link below:\n"
                + verificationLink + "\n\n"
                + "If you did not initiate this registration, please ignore this message.\n";
    }

    private String buildInstructorWelcomeBody(String recipientName, String temporaryPassword, String loginUrl) {
        return "Hi " + recipientName + ",\n\n"
                + "You have been onboarded as an Instructor on Talent Flow.\n"
                + "Temporary password: " + temporaryPassword + "\n"
                + "Login URL: " + loginUrl + "\n\n"
                + "Please sign in and change your password immediately.\n";
    }

    private String buildPasswordResetBody(String recipientName, String resetLink) {
        return "Hi " + recipientName + ",\n\n"
                + "A password reset has been initiated for your Talent Flow account.\n"
                + "Use this link to set a new password:\n"
                + resetLink + "\n\n"
                + "If you did not request this, contact an administrator immediately.\n";
    }
}
