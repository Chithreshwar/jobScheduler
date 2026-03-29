package com.scheduler.enums;

import com.scheduler.domain.Job;

/**
 * Lifecycle state for a {@link Job} definition, not individual run outcomes.
 * Run outcomes (STARTED, SUCCESS, FAILED) belong in {@link com.scheduler.domain.JobExecution} /
 * {@link ExecutionStatus}.
 * <p>
 * Scheduling is controlled by {@link Job#getNextExecutionTime()} while the job is {@link #ACTIVE}, not by
 * status transitions such as a separate "scheduled" state.
 */
public enum JobStatus {
    /** Job is eligible to run when {@code nextExecutionTime <= now} (and not superseded by business rules). */
    ACTIVE,
    /** Job is disabled and will not be picked by the scheduler. */
    DISABLED,
    /** Job has failed permanently (e.g. exceeded max retries); may be moved to DLQ. */
    FAILED
}
