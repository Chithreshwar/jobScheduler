-- Align stored job status values with simplified JobStatus enum (ACTIVE, DISABLED, FAILED).
-- Legacy SCHEDULED/RUNNING meant "in scheduling pipeline" / "executing" but scheduling is driven by next_execution_time;
-- execution state lives in job_executions.

UPDATE jobs SET status = 'ACTIVE' WHERE status IN ('SCHEDULED', 'RUNNING');
UPDATE jobs SET status = 'DISABLED' WHERE status = 'PAUSED';
