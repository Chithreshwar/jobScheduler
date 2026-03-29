package com.scheduler.api.dto;

import com.scheduler.domain.Job;
import com.scheduler.enums.JobPriority;
import com.scheduler.enums.JobStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JobResponse {
    private UUID id;
    private String name;
    private String cronExpression;
    private JobStatus status;
    private JobPriority priority;
    private LocalDateTime nextExecutionTime;
    private int retryCount;
    private int maxRetries;
    private String payload;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JobResponse from(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setName(job.getName());
        response.setCronExpression(job.getCronExpression());
        response.setStatus(job.getStatus());
        response.setPriority(job.getPriority());
        response.setNextExecutionTime(job.getNextExecutionTime());
        response.setRetryCount(job.getRetryCount());
        response.setMaxRetries(job.getMaxRetries());
        response.setPayload(job.getPayload());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}
