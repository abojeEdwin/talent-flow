package com.talentFlow.auth.infrastructure.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthMailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

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

    private String buildVerificationBody(String recipientName, String verificationLink) {
        return "Hi " + recipientName + ",\n\n"
                + "Welcome to Talent Flow.\n"
                + "Please verify your account using the link below:\n"
                + verificationLink + "\n\n"
                + "If you did not initiate this registration, please ignore this message.\n";
    }
}
