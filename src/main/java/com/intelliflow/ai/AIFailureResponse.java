package com.intelliflow.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIFailureResponse {

    private String rootCause;
    private String suggestion;
    private boolean shouldRetry;
    private String severity;
}
