package com.scheduler.api.dto;

import com.scheduler.domain.JobPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateJobRequest {
    
    @NotBlank(message = "Job name is required")
    private String name;
    
    @NotBlank(message = "Cron expression is required")
    private String cronExpression;
    
    @NotNull(message = "Max retries is required")
    @Min(value = 0, message = "Max retries must be non-negative")
    private Integer maxRetries;
    
    private String payload;

    /** Optional; defaults to MEDIUM when omitted. */
    private JobPriority priority;
}
