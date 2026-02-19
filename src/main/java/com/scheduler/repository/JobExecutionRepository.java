package com.scheduler.repository;

import com.scheduler.domain.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {
}

