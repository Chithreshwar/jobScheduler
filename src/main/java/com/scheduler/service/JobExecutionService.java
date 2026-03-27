package com.scheduler.service;

import com.scheduler.domain.Job;
import org.springframework.scheduling.annotation.Async;

public interface JobExecutionService {

    @Async("jobExecutor")
    void executeJob(Job job);
}
