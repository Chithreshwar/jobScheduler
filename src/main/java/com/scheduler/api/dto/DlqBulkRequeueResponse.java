package com.scheduler.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DlqBulkRequeueResponse {
    private int requeuedCount;
    private List<RequeuedItem> items;

    @Data
    @Builder
    public static class RequeuedItem {
        private Long dlqId;
        private UUID newJobId;
    }
}
