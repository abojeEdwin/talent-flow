package com.talentFlow.auth.infrastructure.mail.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundEmailWorker {

    private final OutboundEmailJobRepository outboundEmailJobRepository;
    private final JavaMailSender mailSender;
    private final PlatformTransactionManager transactionManager;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Scheduled(fixedDelay = 5000)
    public void processPendingEmails() {
        List<OutboundEmailJob> jobs = outboundEmailJobRepository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        EmailJobStatus.PENDING, LocalDateTime.now());

        if (!jobs.isEmpty()) {
            log.info("Processing {} pending email job(s)", jobs.size());
        }

        for (OutboundEmailJob job : jobs) {
            processSingleJob(job.getId());
        }
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void recoverStuckJobs() {
        LocalDateTime staleCutoff = LocalDateTime.now().minusMinutes(5);
        List<OutboundEmailJob> staleJobs = outboundEmailJobRepository
                .findTop20ByStatusAndUpdatedAtLessThanEqualOrderByUpdatedAtAsc(
                        EmailJobStatus.PROCESSING, staleCutoff);

        for (OutboundEmailJob staleJob : staleJobs) {
            staleJob.setStatus(EmailJobStatus.PENDING);
            staleJob.setNextAttemptAt(LocalDateTime.now());
            staleJob.setLastError("Recovered from stale PROCESSING state after worker interruption");
            outboundEmailJobRepository.save(staleJob);
            log.warn("Recovered stale outbound email job {}", staleJob.getId());
        }
    }

    public void processSingleJob(UUID jobId) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.execute(status -> {
            OutboundEmailJob job = outboundEmailJobRepository.findById(jobId).orElse(null);
            if (job == null || job.getStatus() != EmailJobStatus.PENDING) {
                return null;
            }

            try {
                job.setStatus(EmailJobStatus.PROCESSING);
                job.setAttempts(job.getAttempts() + 1);
                outboundEmailJobRepository.save(job);

                if (job.getType() == EmailJobType.VERIFICATION) {
                    job.setStatus(EmailJobStatus.COMPLETED);
                    job.setLastError("Email verification is disabled");
                    outboundEmailJobRepository.save(job);
                    log.info("Skipped legacy verification email job {}", job.getId());
                    return null;
                }

                String from = resolveFromAddress();
                String to   = job.getRecipientEmail();
                String subj = resolveSubject(job);

                log.info("Sending email job {} | type={} | from={} | to={} | subject={}",
                        job.getId(), job.getType(), from, to, subj);

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(to);
                message.setSubject(subj);
                message.setText(resolveBody(job));
                mailSender.send(message);

                log.info("Email job {} COMPLETED — sent to {}", job.getId(), to);

                job.setStatus(EmailJobStatus.COMPLETED);
                job.setLastError(null);
                outboundEmailJobRepository.save(job);

            } catch (Exception exception) {
                log.error("Email job {} FAILED on attempt {}: {}",
                        job.getId(), job.getAttempts(), exception.getMessage(), exception);
                handleFailure(job, buildErrorMessage(exception));
            }

            return null;
        });
    }

    private String resolveSubject(OutboundEmailJob job) {
        return switch (job.getType()) {
            case VERIFICATION       -> "Verify your Talent Flow account";
            case INSTRUCTOR_WELCOME -> "Welcome to Talent Flow - Instructor Onboarding";
            case PASSWORD_RESET     -> "Talent Flow password reset";
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
        String configured = fromAddress == null ? "" : fromAddress.trim();
        if (!configured.isBlank()) {
            return configured;
        }
        throw new IllegalStateException("Missing valid app.mail.from; configure a verified sender email");
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
            log.error("Email job {} permanently FAILED after {} attempts. Last error: {}",
                    job.getId(), job.getAttempts(), errorMessage);
        } else {
            job.setStatus(EmailJobStatus.PENDING);
            long backoffSeconds = (long) Math.pow(2, Math.max(1, job.getAttempts()));
            job.setNextAttemptAt(LocalDateTime.now().plusSeconds(backoffSeconds));
            log.warn("Email job {} will retry in {}s (attempt {}/{})",
                    job.getId(), backoffSeconds, job.getAttempts(), job.getMaxAttempts());
        }
        outboundEmailJobRepository.save(job);
    }
}
