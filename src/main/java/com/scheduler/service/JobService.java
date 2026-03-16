package com.scheduler.service;

import com.scheduler.api.dto.CreateJobRequest;
import com.scheduler.api.dto.JobResponse;
import com.scheduler.domain.Job;
import com.scheduler.domain.JobStatus;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        LocalDateTime nextExecutionTime = LocalDateTime.now();

        Job job = Job.builder()
                .name(request.getName())
                .cronExpression(request.getCronExpression())
                .status(JobStatus.ACTIVE)
                .retryCount(0)
                .maxRetries(request.getMaxRetries())
                .payload(request.getPayload())
                .nextExecutionTime(nextExecutionTime)
                .build();

        log.info("Persisting new job: name={}, cron={}, maxRetries={}, status={}, nextExecutionTime={}",
                job.getName(), job.getCronExpression(), job.getMaxRetries(),
                job.getStatus(), job.getNextExecutionTime());

        job = jobRepository.save(job);

        log.info("Job persisted: id={}, name={}, status={}, nextExecutionTime={}",
                job.getId(), job.getName(), job.getStatus(), job.getNextExecutionTime());

        return JobResponse.from(job);
    }
}
