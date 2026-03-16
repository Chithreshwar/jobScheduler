package com.scheduler.repository;

import com.scheduler.domain.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
}

