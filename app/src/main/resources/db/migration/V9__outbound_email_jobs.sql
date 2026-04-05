CREATE TABLE outbound_email_jobs (
    id UUID PRIMARY KEY,
    type VARCHAR(40) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(120) NOT NULL,
    link VARCHAR(500),
    temporary_password VARCHAR(200),
    login_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    next_attempt_at TIMESTAMP NOT NULL,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_outbound_email_jobs_status_next_attempt
    ON outbound_email_jobs(status, next_attempt_at);
