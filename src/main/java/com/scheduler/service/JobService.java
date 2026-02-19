package com.scheduler.service;

import com.scheduler.api.dto.CreateJobRequest;
import com.scheduler.api.dto.JobResponse;
import com.scheduler.domain.Job;
import com.scheduler.domain.JobStatus;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        Job job = Job.builder()
                .name(request.getName())
                .cronExpression(request.getCronExpression())
                .status(JobStatus.ACTIVE)
                .retryCount(0)
                .maxRetries(request.getMaxRetries())
                .payload(request.getPayload())
                .nextExecutionTime(LocalDateTime.now())
                .build();

        job = jobRepository.save(job);
        return JobResponse.from(job);
    }
}
