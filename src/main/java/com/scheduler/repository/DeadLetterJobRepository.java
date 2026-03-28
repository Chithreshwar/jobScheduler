package com.scheduler.repository;

import com.scheduler.domain.DeadLetterJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeadLetterJobRepository extends JpaRepository<DeadLetterJob, Long> {

    Optional<DeadLetterJob> findByJobIdAndRequeuedIsFalse(UUID jobId);

    List<DeadLetterJob> findAllByOrderByFailedAtDesc();

    List<DeadLetterJob> findByRequeuedFalseOrderByFailedAtAsc();
}
