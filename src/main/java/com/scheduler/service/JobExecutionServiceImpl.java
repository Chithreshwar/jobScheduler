package com.scheduler.service;

import com.scheduler.domain.Job;
import com.scheduler.domain.JobExecution;
import com.scheduler.domain.ExecutionStatus;
import com.scheduler.repository.JobExecutionRepository;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutionServiceImpl implements JobExecutionService {

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;

    @Override
    @Transactional
    public void executeJob(Job job) {
        log.info("Running job {} in thread {}", job.getId(), Thread.currentThread().getName());

        LocalDateTime now = LocalDateTime.now();

        log.info("Starting execution for job: id={}, name={}", job.getId(), job.getName());

        // 1. Create JobExecution entity with STARTED status
        JobExecution execution = JobExecution.builder()
                .job(job)
                .status(ExecutionStatus.STARTED)
                .startTime(now)
                .retryAttempt(job.getRetryCount())
                .build();

        execution = jobExecutionRepository.save(execution);

        try {
            // 2. Simulate execution: print payload and sleep 1 second
            log.info("Executing job: {} - Payload: {}", job.getName(), job.getPayload());
            Thread.sleep(1000);

            // 3. Simulate random failure (30% chance)
            boolean success = Math.random() > 0.3;

            if (success) {
                // Success case
                execution.setStatus(ExecutionStatus.SUCCESS);
                execution.setEndTime(LocalDateTime.now());

                job.setRetryCount(0);
                job.setNextExecutionTime(computeNextExecutionTime(job.getCronExpression(), now));
                jobRepository.save(job);

                log.info("Job {} executed successfully. Next execution: {}", job.getName(), job.getNextExecutionTime());
            } else {
                // Failure case
                execution.setStatus(ExecutionStatus.FAILED);
                execution.setEndTime(LocalDateTime.now());
                execution.setErrorMessage("Simulated execution failure");

                int retryCount = job.getRetryCount() + 1;
                job.setRetryCount(retryCount);

                if (retryCount <= job.getMaxRetries()) {
                    long delaySeconds;
                    switch (retryCount) {
                        case 1 -> delaySeconds = 10L;
                        case 2 -> delaySeconds = 30L;
                        case 3 -> delaySeconds = 60L;
                        default -> delaySeconds = 60L;
                    }

                    LocalDateTime nextTime = now.plusSeconds(delaySeconds);
                    job.setNextExecutionTime(nextTime);
                    log.info("Job failed. Scheduling retry {} in {} seconds", retryCount, delaySeconds);
                } else {
                    job.setStatus(com.scheduler.domain.JobStatus.FAILED);
                    log.warn("Job {} exceeded max retries. Marking FAILED", job.getName());
                }

                jobRepository.save(job);
            }

            jobExecutionRepository.save(execution);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setEndTime(LocalDateTime.now());
            execution.setErrorMessage(e.getMessage());
            jobExecutionRepository.save(execution);
            log.error("Job {} execution interrupted", job.getName(), e);
        } catch (Exception e) {
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setEndTime(LocalDateTime.now());
            execution.setErrorMessage(e.getMessage());
            jobExecutionRepository.save(execution);
            log.error("Job {} execution failed with error", job.getName(), e);
        }
    }

    private LocalDateTime computeNextExecutionTime(String cronExpression, LocalDateTime currentTime) {
        try {
            return CronExpression.parse(cronExpression)
                    .next(currentTime.atZone(ZoneId.systemDefault()))
                    .toLocalDateTime();
        } catch (Exception e) {
            log.error("Failed to parse cron expression: {}", cronExpression, e);
            // Fallback: return current time + 1 hour
            return currentTime.plusHours(1);
        }
    }
}
