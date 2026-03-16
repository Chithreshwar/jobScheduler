package com.scheduler.repository;

import com.scheduler.domain.Job;
import com.scheduler.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findTop100ByStatusAndNextExecutionTimeLessThanEqualOrderByNextExecutionTimeAsc(
            JobStatus status,
            LocalDateTime time
    );

    @Modifying
    @Transactional
    @Query("""
            update Job j
            set j.status = com.scheduler.domain.JobStatus.RUNNING
            where j.id = :id
              and j.status = com.scheduler.domain.JobStatus.SCHEDULED
            """)
    int markJobRunningIfScheduled(@Param("id") UUID id);
}

