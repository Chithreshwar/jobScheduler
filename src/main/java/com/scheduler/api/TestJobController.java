package com.scheduler.api;

import com.scheduler.service.core.JobExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * This endpoint is for testing distributed locking and should not be used in production.
 */
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestJobController {

    private final JobExecutionService jobExecutionService;

    @PostMapping("/run-concurrent/{jobId}")
    public String runConcurrent(@PathVariable UUID jobId,
                                @RequestParam(defaultValue = "5") int count) {
        log.info("Triggering concurrent test executions: jobId={}, count={}", jobId, count);

        for (int i = 0; i < count; i++) {
            jobExecutionService.executeJobAsync(jobId);
        }

        log.info("Fired {} non-blocking async execution(s) for jobId={}", count, jobId);

        return "Triggered " + count + " concurrent executions for jobId=" + jobId;
    }
}
