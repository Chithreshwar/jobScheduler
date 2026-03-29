package com.scheduler.repository;

import com.scheduler.domain.Job;
import com.scheduler.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findTop100ByStatusAndNextExecutionTimeLessThanEqualOrderByPriorityDescNextExecutionTimeAsc(
            JobStatus status,
            LocalDateTime time
    );
}

