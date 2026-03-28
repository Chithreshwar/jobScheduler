package com.scheduler.api.dto;

import com.scheduler.domain.DeadLetterJob;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DlqEntryResponse {
    private Long id;
    private UUID jobId;
    private String jobName;
    private String cronExpression;
    private Integer maxRetries;
    private String payload;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime failedAt;
    private boolean requeued;
    private LocalDateTime createdAt;

    public static DlqEntryResponse from(DeadLetterJob entity) {
        return DlqEntryResponse.builder()
                .id(entity.getId())
                .jobId(entity.getJobId())
                .jobName(entity.getJobName())
                .cronExpression(entity.getCronExpression())
                .maxRetries(entity.getMaxRetries())
                .payload(entity.getPayload())
                .errorMessage(entity.getErrorMessage())
                .retryCount(entity.getRetryCount())
                .failedAt(entity.getFailedAt())
                .requeued(entity.isRequeued())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
