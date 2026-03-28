package com.scheduler.service.exception;

public class DlqEntryNotFoundException extends RuntimeException {

    public DlqEntryNotFoundException(Long dlqId) {
        super("DLQ entry not found: id=" + dlqId);
    }
}
