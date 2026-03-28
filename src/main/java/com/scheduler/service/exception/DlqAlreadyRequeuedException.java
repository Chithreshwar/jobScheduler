package com.scheduler.service.exception;

public class DlqAlreadyRequeuedException extends RuntimeException {

    public DlqAlreadyRequeuedException(Long dlqId) {
        super("DLQ entry already requeued: id=" + dlqId);
    }
}
