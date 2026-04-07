UPDATE users
SET status = 'ACTIVE',
    email_verified = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'PENDING_VERIFICATION'
   OR email_verified = FALSE;

UPDATE outbound_email_jobs
SET status = 'COMPLETED',
    last_error = 'Skipped because email verification is disabled',
    updated_at = CURRENT_TIMESTAMP
WHERE type = 'VERIFICATION'
  AND status IN ('PENDING', 'PROCESSING');

DROP TABLE IF EXISTS verification_tokens;
