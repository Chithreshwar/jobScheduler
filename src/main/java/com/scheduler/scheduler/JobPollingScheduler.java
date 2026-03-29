package com.scheduler.scheduler;

import com.scheduler.domain.Job;
import com.scheduler.domain.JobStatus;
import com.scheduler.repository.JobRepository;
import com.scheduler.service.JobExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls for jobs that are due for execution. Which runs are due is determined by
 * {@link Job#getNextExecutionTime()} and {@link JobStatus#ACTIVE}, not by moving the job through a
 * "scheduled" status — execution attempts are tracked in {@code job_executions}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobPollingScheduler {

    private final JobRepository jobRepository;
    private final JobExecutionService jobExecutionService;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollScheduledJobs() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("JobPollingScheduler tick at={}", now);

        List<Job> dueJobs =
                jobRepository.findTop100ByStatusAndNextExecutionTimeLessThanEqualOrderByPriorityDescNextExecutionTimeAsc(
                        JobStatus.ACTIVE,
                        now
                );

        log.info("JobPollingScheduler picked {} active job(s) due for execution", dueJobs.size());

        for (Job job : dueJobs) {
            tryExecuteJob(job);
        }
    }

    private void tryExecuteJob(Job job) {
        log.info("Scheduling job id={}, priority={}", job.getId(), job.getPriority());
        log.info("Attempting to execute job: id={}, name={}", job.getId(), job.getName());

        try {
            jobExecutionService.executeJobAsync(job.getId());
        } catch (Exception e) {
            log.error("Error submitting async execution for job id={}", job.getId(), e);
        }
    }
}

