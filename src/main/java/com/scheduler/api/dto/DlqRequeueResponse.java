package com.scheduler.api.dto;

import com.scheduler.domain.Job;
import com.scheduler.enums.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DlqRequeueResponse {
    private Long dlqId;
    private UUID jobId;
    private JobStatus status;
    private String message;

    public static DlqRequeueResponse of(Long dlqId, Job newJob) {
        return DlqRequeueResponse.builder()
                .dlqId(dlqId)
                .jobId(newJob.getId())
                .status(newJob.getStatus())
                .message("Job requeued successfully; new job is ACTIVE and due now")
                .build();
    }
}
