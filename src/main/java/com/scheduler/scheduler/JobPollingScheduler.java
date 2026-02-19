package com.scheduler.scheduler;

import com.scheduler.domain.Job;
import com.scheduler.domain.JobStatus;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobPollingScheduler {

    private final JobRepository jobRepository;

    @Scheduled(fixedDelay = 5000)
    public void pollScheduledJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<Job> scheduledJobs =
                jobRepository.findTop100ByStatusAndNextExecutionTimeLessThanEqualOrderByNextExecutionTimeAsc(
                        JobStatus.SCHEDULED,
                        now
                );

        log.debug("JobPollingScheduler found {} scheduled jobs ready for execution", scheduledJobs.size());
    }
}

