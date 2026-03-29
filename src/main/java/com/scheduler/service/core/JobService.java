package com.scheduler.service.core;

import com.scheduler.api.dto.CreateJobRequest;
import com.scheduler.api.dto.JobResponse;
import com.scheduler.domain.Job;
import com.scheduler.enums.JobPriority;
import com.scheduler.enums.JobStatus;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        LocalDateTime nextExecutionTime = LocalDateTime.now();

        JobPriority priority = Optional.ofNullable(request.getPriority()).orElse(JobPriority.MEDIUM);

        Job job = Job.builder()
                .name(request.getName())
                .cronExpression(request.getCronExpression())
                .status(JobStatus.ACTIVE)
                .priority(priority)
                .retryCount(0)
                .maxRetries(request.getMaxRetries())
                .payload(request.getPayload())
                .nextExecutionTime(nextExecutionTime)
                .build();

        log.info("Persisting new job: name={}, cron={}, maxRetries={}, status={}, priority={}, nextExecutionTime={}",
                job.getName(), job.getCronExpression(), job.getMaxRetries(),
                job.getStatus(), job.getPriority(), job.getNextExecutionTime());

        job = jobRepository.save(job);

        log.info("Job persisted: id={}, name={}, status={}, priority={}, nextExecutionTime={}",
                job.getId(), job.getName(), job.getStatus(), job.getPriority(), job.getNextExecutionTime());

        return JobResponse.from(job);
    }
}
