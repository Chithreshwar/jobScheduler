package com.scheduler.service.dlq.exception;

public class DlqAlreadyRequeuedException extends RuntimeException {

    public DlqAlreadyRequeuedException(Long dlqId) {
        super("DLQ entry already requeued: id=" + dlqId);
    }
}
