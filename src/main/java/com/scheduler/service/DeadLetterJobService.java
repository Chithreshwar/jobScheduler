package com.scheduler.service;

import com.scheduler.domain.DeadLetterJob;
import com.scheduler.domain.Job;

import java.util.List;

public interface DeadLetterJobService {

    DeadLetterJob moveToDLQ(Job job, String errorMessage);

    Job requeueFromDLQ(Long dlqId);

    List<DeadLetterJob> findAllDlqEntries();

    List<DlqRequeueOutcome> requeueAllPending();
}
