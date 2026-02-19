package com.scheduler.service;

import com.scheduler.domain.Job;

public interface JobExecutionService {
    void executeJob(Job job);
}
