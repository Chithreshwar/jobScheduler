package com.scheduler.service;

import com.scheduler.domain.Job;

public record DlqRequeueOutcome(Long dlqId, Job newJob) {
}
