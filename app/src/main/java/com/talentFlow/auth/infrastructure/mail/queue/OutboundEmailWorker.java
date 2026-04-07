package com.talentFlow.auth.infrastructure.mail.queue;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundEmailWorker {

    private final OutboundEmailJobRepository outboundEmailJobRepository;


    //EMAIL_FROM
    @Value("${app.mail.from:onboarding@resend.dev}")
    private String fromAddress;

    //RESEND_API_KEY
    @Value("${app.mail.resend-api-key}")
    private String resendApiKey;

    @Scheduled(fixedDelay = 5000)
    public void processPendingEmails() {
        List<OutboundEmailJob> jobs = outboundEmailJobRepository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(EmailJobStatus.PENDING, LocalDateTime.now());

        for (OutboundEmailJob job : jobs) {
            processSingleJob(job.getId());
        }
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void recoverStuckJobs() {
        LocalDateTime staleCutoff = LocalDateTime.now().minusMinutes(5);
        List<OutboundEmailJob> staleJobs = outboundEmailJobRepository
                .findTop20ByStatusAndUpdatedAtLessThanEqualOrderByUpdatedAtAsc(EmailJobStatus.PROCESSING, staleCutoff);

        for (OutboundEmailJob staleJob : staleJobs) {
            staleJob.setStatus(EmailJobStatus.PENDING);
            staleJob.setNextAttemptAt(LocalDateTime.now());
            staleJob.setLastError("Recovered from stale PROCESSING state after worker interruption");
            outboundEmailJobRepository.save(staleJob);
            log.warn("Recovered stale outbound email job {}", staleJob.getId());
        }
    }

    @Transactional
    public void processSingleJob(UUID jobId) {
        OutboundEmailJob job = outboundEmailJobRepository.findById(jobId).orElse(null);
        if (job == null || job.getStatus() != EmailJobStatus.PENDING) {
            return;
        }

        try {
            job.setStatus(EmailJobStatus.PROCESSING);
            job.setAttempts(job.getAttempts() + 1);
            outboundEmailJobRepository.save(job);

            if (job.getType() == EmailJobType.VERIFICATION) {
                job.setStatus(EmailJobStatus.COMPLETED);
                job.setLastError("Email verification is disabled");
                outboundEmailJobRepository.save(job);
                log.info("Skipped legacy verification email job {} because verification is disabled", job.getId());
                return;
            }

            Resend resend = new Resend(resolveResendApiKey());
            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(resolveFromAddress())
                    .to(job.getRecipientEmail())
                    .subject(resolveSubject(job))
                    .text(resolveBody(job))
                    .build();
            var response = resend.emails().send(email);
            log.info("Outbound email job {} sent via Resend with id {}", job.getId(), response.getId());

            job.setStatus(EmailJobStatus.COMPLETED);
            job.setLastError(null);
            outboundEmailJobRepository.save(job);
        } catch (Exception exception) {
            log.warn(
                    "Outbound email job {} failed on attempt {}: {}",
                    job.getId(),
                    job.getAttempts(),
                    exception.getMessage()
            );
            handleFailure(job, buildErrorMessage(exception));
        }
    }

    private String resolveSubject(OutboundEmailJob job) {
        return switch (job.getType()) {
            case VERIFICATION -> "Verify your Talent Flow account";
            case INSTRUCTOR_WELCOME -> "Welcome to Talent Flow - Instructor Onboarding";
            case PASSWORD_RESET -> "Talent Flow password reset";
        };
    }

    private String resolveBody(OutboundEmailJob job) {
        return switch (job.getType()) {
            case VERIFICATION -> "Hi " + job.getRecipientName() + ",\n\n"
                    + "Welcome to Talent Flow.\n"
                    + "Please verify your account using the link below:\n"
                    + nullSafe(job.getLink()) + "\n\n"
                    + "If you did not initiate this registration, please ignore this message.\n";
            case INSTRUCTOR_WELCOME -> "Hi " + job.getRecipientName() + ",\n\n"
                    + "You have been onboarded as an Instructor on Talent Flow.\n"
                    + "Temporary password: " + nullSafe(job.getTemporaryPassword()) + "\n"
                    + "Login URL: " + nullSafe(job.getLoginUrl()) + "\n\n"
                    + "Please sign in and change your password immediately.\n";
            case PASSWORD_RESET -> "Hi " + job.getRecipientName() + ",\n\n"
                    + "A password reset has been initiated for your Talent Flow account.\n"
                    + "Use this link to set a new password:\n"
                    + nullSafe(job.getLink()) + "\n\n"
                    + "If you did not request this, contact an administrator immediately.\n";
        };
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String resolveFromAddress() {
        String configuredFrom = fromAddress == null ? "" : fromAddress.trim();
        if (!configuredFrom.isBlank()) {
            return configuredFrom;
        }
        throw new IllegalStateException("Missing valid EMAIL_FROM; configure a verified sender email");
    }

    private String resolveResendApiKey() {
        String configuredApiKey = resendApiKey == null ? "" : resendApiKey.trim();
        if (!configuredApiKey.isBlank()) {
            return configuredApiKey;
        }
        throw new IllegalStateException("Missing RESEND_API_KEY; configure Resend API key for HTTP email delivery");
    }

    private String buildErrorMessage(Exception exception) {
        Throwable root = exception;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String type = root.getClass().getSimpleName();
        String message = root.getMessage();
        return message == null || message.isBlank() ? type : type + ": " + message;
    }

    private void handleFailure(OutboundEmailJob job, String errorMessage) {
        job.setLastError(errorMessage);
        if (job.getAttempts() >= job.getMaxAttempts()) {
            job.setStatus(EmailJobStatus.FAILED);
        } else {
            job.setStatus(EmailJobStatus.PENDING);
            long backoffSeconds = (long) Math.pow(2, Math.max(1, job.getAttempts()));
            job.setNextAttemptAt(LocalDateTime.now().plusSeconds(backoffSeconds));
        }
        outboundEmailJobRepository.save(job);
    }
}
