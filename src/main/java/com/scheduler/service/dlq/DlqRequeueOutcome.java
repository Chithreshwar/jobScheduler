package com.scheduler.service.dlq;

import com.scheduler.domain.Job;

public record DlqRequeueOutcome(Long dlqId, Job newJob) {
}
