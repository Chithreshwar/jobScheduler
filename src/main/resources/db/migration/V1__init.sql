-- Flyway migration V1: initial schema for distributed-job-scheduler

CREATE TABLE jobs (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(255)        NOT NULL,
    cron_expression     VARCHAR(255)        NOT NULL,
    status              VARCHAR(50)         NOT NULL,
    next_execution_time TIMESTAMP WITHOUT TIME ZONE,
    retry_count         INTEGER             NOT NULL,
    max_retries         INTEGER             NOT NULL,
    payload             TEXT,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_jobs_next_execution_time
    ON jobs (next_execution_time);

CREATE INDEX idx_jobs_status
    ON jobs (status);

CREATE TABLE job_executions (
    id            UUID PRIMARY KEY,
    job_id        UUID                     NOT NULL,
    status        VARCHAR(50)              NOT NULL,
    retry_attempt INTEGER                  NOT NULL,
    start_time    TIMESTAMP WITHOUT TIME ZONE,
    end_time      TIMESTAMP WITHOUT TIME ZONE,
    error_message TEXT,
    CONSTRAINT fk_job_execution_job
        FOREIGN KEY (job_id)
            REFERENCES jobs (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_job_executions_job_id
    ON job_executions (job_id);

CREATE INDEX idx_job_executions_status
    ON job_executions (status);

