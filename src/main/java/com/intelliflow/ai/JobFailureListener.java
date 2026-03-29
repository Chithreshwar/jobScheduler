package com.intelliflow.ai;

import com.scheduler.domain.Job;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobFailureListener {

    private final AIFailureAnalyzer aiFailureAnalyzer;
    private final JobRepository jobRepository;

    @Async("aiExecutor")
    @EventListener
    public void handleJobFailure(JobFailureEvent event) {
        try {
            log.info("AI analysis started for jobId={}", event.getJobId());

            AIFailureResponse response =
                    aiFailureAnalyzer.analyzeFailure(
                            event.getErrorMessage(),
                            event.getStackTrace(),
                            event.getJobType()
                    );

            Job job = jobRepository.findById(event.getJobId()).orElse(null);
            if (job == null) {
                log.warn("Job not found for AI update, jobId={}", event.getJobId());
                return;
            }

            job.setAiRootCause(response.getRootCause());
            job.setAiSuggestion(response.getSuggestion());
            job.setAiSeverity(response.getSeverity());
            job.setAiDecision(response.isShouldRetry());

            jobRepository.save(job);

            log.info("AI analysis completed for jobId={}", event.getJobId());

        } catch (Exception e) {
            log.error("AI processing failed for jobId={}", event.getJobId(), e);
        }
    }
}
