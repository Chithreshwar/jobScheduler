package com.intelliflow.ai;

public interface AIFailureAnalyzer {

    AIFailureResponse analyzeFailure(String errorMessage, String stackTrace, String jobType);
}
