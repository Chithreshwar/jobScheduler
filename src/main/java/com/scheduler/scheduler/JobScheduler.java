package com.scheduler.scheduler;

import com.scheduler.domain.Job;
import com.scheduler.domain.JobStatus;
import com.scheduler.repository.JobRepository;
import com.scheduler.service.JobExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduler {

    private final JobRepository jobRepository;
    private final JobExecutionService jobExecutionService;

    @Scheduled(fixedRate = 5000)
    public void scheduleJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<Job> jobsToExecute = jobRepository
                .findTop100ByStatusAndNextExecutionTimeLessThanEqualOrderByNextExecutionTimeAsc(
                        JobStatus.ACTIVE,
                        now
                );

        log.debug("Found {} jobs to execute", jobsToExecute.size());

        for (Job job : jobsToExecute) {
            jobExecutionService.executeJob(job);
        }
    }
}
