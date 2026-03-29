package com.scheduler.service.core;

import com.scheduler.domain.Job;

import java.util.UUID;

public interface JobExecutionService {

    void executeJob(Job job);

    void executeJobAsync(UUID jobId);
}
