package com.scheduler.api;

import com.scheduler.api.dto.CreateJobRequest;
import com.scheduler.api.dto.JobResponse;
import com.scheduler.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        log.info("Received createJob request: name={}, cron={}, maxRetries={}",
                request.getName(), request.getCronExpression(), request.getMaxRetries());

        JobResponse response = jobService.createJob(request);

        log.info("Job created successfully: id={}, name={}, cron={}",
                response.getId(), response.getName(), response.getCronExpression());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
