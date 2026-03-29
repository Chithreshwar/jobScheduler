package com.intelliflow.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class JobFailureEvent {
    private UUID jobId;
    private String errorMessage;
    private String stackTrace;
    private String jobType;
}
