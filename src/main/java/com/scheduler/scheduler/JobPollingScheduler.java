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
                jobRepository.findTop100ByStatusAndNextExecutionTimeLessThanEqualOrderByNextExecutionTimeAsc(
                        JobStatus.ACTIVE,
                        now
                );

        log.info("JobPollingScheduler picked {} active job(s) due for execution", dueJobs.size());
        if (!dueJobs.isEmpty()) {
            log.info("Scheduling job id(s): {}", dueJobs.stream().map(Job::getId).toList());
        }

        for (Job job : dueJobs) {
            job.setStatus(JobStatus.SCHEDULED);
            tryExecuteJob(job);
        }
    }

    private void tryExecuteJob(Job job) {
        log.info("Attempting to execute job: id={}, name={}", job.getId(), job.getName());

        try {
            jobExecutionService.executeJob(job);
        } catch (Exception e) {
            log.error("Error executing job id={}", job.getId(), e);
        }
    }
}

