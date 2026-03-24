ALTER TABLE course_materials
    ALTER COLUMN content_url DROP NOT NULL;

ALTER TABLE course_materials
    ADD COLUMN upload_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED';

CREATE TABLE media_upload_jobs (
    id UUID PRIMARY KEY,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    bucket_folder VARCHAR(120) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(120),
    payload BYTEA,
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    next_attempt_at TIMESTAMP NOT NULL,
    uploaded_url VARCHAR(500),
    last_error TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_media_upload_jobs_status_next_attempt
    ON media_upload_jobs(status, next_attempt_at);
