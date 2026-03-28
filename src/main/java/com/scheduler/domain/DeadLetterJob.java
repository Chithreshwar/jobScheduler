package com.scheduler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "dead_letter_jobs",
        indexes = {
                @Index(name = "idx_dead_letter_jobs_failed_at", columnList = "failed_at"),
                @Index(name = "idx_dead_letter_jobs_requeued", columnList = "requeued")
        }
)
public class DeadLetterJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** Original {@link Job} id that exhausted retries. */
    @Column(name = "source_job_id", nullable = false, updatable = false)
    private UUID jobId;

    @Column(name = "job_name", nullable = false, updatable = false)
    private String jobName;

    @Column(name = "cron_expression", nullable = false, updatable = false)
    private String cronExpression;

    @Column(name = "max_retries", nullable = false, updatable = false)
    private Integer maxRetries;

    @Column(name = "payload", columnDefinition = "TEXT", updatable = false)
    private String payload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** {@link Job#getRetryCount()} at the time of terminal failure (may be refreshed on idempotent DLQ move). */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "failed_at", nullable = false)
    private LocalDateTime failedAt;

    @Column(name = "requeued", nullable = false)
    @Builder.Default
    private boolean requeued = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
