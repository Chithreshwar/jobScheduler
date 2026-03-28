-- Dead letter queue: terminal failures after max retries; supports requeue as a new job row.

CREATE TABLE dead_letter_jobs (
    id               BIGSERIAL PRIMARY KEY,
    source_job_id    UUID         NOT NULL,
    job_name         VARCHAR(255) NOT NULL,
    cron_expression  VARCHAR(255) NOT NULL,
    max_retries      INTEGER      NOT NULL,
    payload          TEXT,
    error_message    TEXT,
    retry_count      INTEGER      NOT NULL,
    failed_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    requeued         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_dead_letter_jobs_failed_at ON dead_letter_jobs (failed_at DESC);

CREATE INDEX idx_dead_letter_jobs_requeued ON dead_letter_jobs (requeued);

-- At most one "open" DLQ row per failed source job (idempotent moveToDLQ; history kept after requeued = true).
CREATE UNIQUE INDEX uq_dead_letter_jobs_source_open
    ON dead_letter_jobs (source_job_id)
    WHERE requeued = FALSE;
