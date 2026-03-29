package com.scheduler.service.core;

import com.scheduler.domain.Job;
import com.scheduler.domain.JobExecution;
import com.scheduler.enums.ExecutionStatus;
import com.scheduler.enums.JobStatus;
import com.scheduler.repository.JobExecutionRepository;
import com.scheduler.repository.JobRepository;
import com.scheduler.service.dlq.DeadLetterJobService;
import com.scheduler.service.lock.RedisLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
public class JobExecutionServiceImpl implements JobExecutionService {

    private static final long JOB_LOCK_TTL_MILLIS = 30_000L;

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final DeadLetterJobService deadLetterJobService;
    private final RedisLockService redisLockService;
    private final JobExecutionService self;

    public JobExecutionServiceImpl(
            JobRepository jobRepository,
            JobExecutionRepository jobExecutionRepository,
            DeadLetterJobService deadLetterJobService,
            RedisLockService redisLockService,
            @Lazy JobExecutionService self) {
        this.jobRepository = jobRepository;
        this.jobExecutionRepository = jobExecutionRepository;
        this.deadLetterJobService = deadLetterJobService;
        this.redisLockService = redisLockService;
        this.self = self;
    }

    @Override
    @Async("jobExecutor")
    public void executeJobAsync(UUID jobId) {
        String lockKey = "job-lock:" + jobId;
        boolean acquired;
        try {
            acquired = redisLockService.acquireLock(lockKey, JOB_LOCK_TTL_MILLIS);
        } catch (Exception e) {
            log.error("Redis lock acquire failed for jobId={}, key={}", jobId, lockKey, e);
            return;
        }
        if (!acquired) {
            log.info(
                    "Skipped job execution; distributed lock not acquired (another instance or in-flight): jobId={}, key={}",
                    jobId,
                    lockKey
            );
            return;
        }
        log.info("Acquired distributed lock for jobId={}, key={}, ttlMillis={}", jobId, lockKey, JOB_LOCK_TTL_MILLIS);
        try {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                log.warn("Job not found after lock acquired, skipping: jobId={}", jobId);
                return;
            }
            self.executeJob(job);
        } catch (Exception e) {
            log.error("Error during executeJobAsync for jobId={}", jobId, e);
        } finally {
            try {
                redisLockService.releaseLock(lockKey);
                log.info("Released distributed lock for jobId={}, key={}", jobId, lockKey);
            } catch (Exception e) {
                log.warn("Failed to release distributed lock for jobId={}, key={}", jobId, lockKey, e);
            }
        }
    }

    @Override
    @Transactional
    public void executeJob(Job job) {
        log.info("Running job {} in thread {}", job.getId(), Thread.currentThread().getName());

        LocalDateTime now = LocalDateTime.now();

        log.info("Starting execution for job: id={}, name={}", job.getId(), job.getName());

        JobExecution execution = JobExecution.builder()
                .job(job)
                .status(ExecutionStatus.STARTED)
                .startTime(now)
                .retryAttempt(job.getRetryCount())
                .build();

        execution = jobExecutionRepository.save(execution);

        try {
            log.info("Executing job: {} - Payload: {}", job.getName(), job.getPayload());
            Thread.sleep(1000);

            boolean success = Math.random() > 0.3;

            if (success) {
                execution.setStatus(ExecutionStatus.SUCCESS);
                execution.setEndTime(LocalDateTime.now());

                job.setRetryCount(0);
                job.setNextExecutionTime(computeNextExecutionTime(job.getCronExpression(), now));
                jobRepository.save(job);

                log.info("Job {} executed successfully. Next execution: {}", job.getName(), job.getNextExecutionTime());
                jobExecutionRepository.save(execution);
            } else {
                applyFailureAnalysisAndRetry(job, execution, now, "Simulated execution failure");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Job {} execution interrupted", job.getName(), e);
            applyFailureAnalysisAndRetry(job, execution, now, e.getMessage());
        } catch (Exception e) {
            log.error("Job {} execution failed with error", job.getName(), e);
            applyFailureAnalysisAndRetry(job, execution, now, e.getMessage());
        }
    }

    /**
     * Records failure, then retries while {@code retryCount <= maxRetries}; when max retries are exceeded,
     * marks the job {@link JobStatus#FAILED} and moves it to the DLQ.
     */
    private void applyFailureAnalysisAndRetry(
            Job job,
            JobExecution execution,
            LocalDateTime now,
            String errorMessage) {

        String safeMessage = errorMessage != null ? errorMessage : "";

        execution.setStatus(ExecutionStatus.FAILED);
        execution.setEndTime(LocalDateTime.now());
        execution.setErrorMessage(safeMessage);

        int retryCount = job.getRetryCount() + 1;
        job.setRetryCount(retryCount);

        if (retryCount <= job.getMaxRetries()) {
            long delaySeconds = delaySecondsForAttempt(retryCount);
            LocalDateTime nextTime = now.plusSeconds(delaySeconds);
            job.setNextExecutionTime(nextTime);
            log.info("Job failed. Scheduling retry {} in {} seconds", retryCount, delaySeconds);
        } else {
            job.setStatus(JobStatus.FAILED);
            log.warn("Job {} exceeded max retries. Marking FAILED and moving to DLQ", job.getName());
            deadLetterJobService.moveToDLQ(job, formatDlqMessage(safeMessage));
        }

        jobRepository.save(job);
        jobExecutionRepository.save(execution);
    }

    private static String formatDlqMessage(String baseError) {
        return baseError;
    }

    private static long delaySecondsForAttempt(int retryCount) {
        return switch (retryCount) {
            case 1 -> 10L;
            case 2 -> 30L;
            case 3 -> 60L;
            default -> 60L;
        };
    }

    private LocalDateTime computeNextExecutionTime(String cronExpression, LocalDateTime currentTime) {
        try {
            return CronExpression.parse(cronExpression)
                    .next(currentTime.atZone(ZoneId.systemDefault()))
                    .toLocalDateTime();
        } catch (Exception e) {
            log.error("Failed to parse cron expression: {}", cronExpression, e);
            return currentTime.plusHours(1);
        }
    }
}
