package com.scheduler.domain;

import com.scheduler.enums.JobPriority;
import com.scheduler.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "jobs", indexes = {@Index(name = "idx_jobs_next_execution_time", columnList = "next_execution_time"), @Index(name = "idx_jobs_status", columnList = "status")})
@EntityListeners(AuditingEntityListener.class)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    /** Definition lifecycle only; scheduling uses {@link #nextExecutionTime} while ACTIVE. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;

    /**
     * Scheduling order among due jobs (higher first). Persisted as NOT NULL with default MEDIUM;
     * {@link #getPriority()} treats null as MEDIUM for safety.
     */
    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private JobPriority priority = JobPriority.MEDIUM;

    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "ai_root_cause", columnDefinition = "TEXT")
    private String aiRootCause;

    @Column(name = "ai_suggestion", columnDefinition = "TEXT")
    private String aiSuggestion;

    @Column(name = "ai_severity", length = 50)
    private String aiSeverity;

    @Column(name = "ai_decision")
    private Boolean aiDecision;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void normalizePriority() {
        if (priority == null) {
            priority = JobPriority.MEDIUM;
        }
    }

    public JobPriority getPriority() {
        return priority != null ? priority : JobPriority.MEDIUM;
    }
}

