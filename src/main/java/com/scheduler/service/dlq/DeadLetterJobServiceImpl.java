package com.scheduler.service.dlq;

import com.scheduler.domain.DeadLetterJob;
import com.scheduler.domain.Job;
import com.scheduler.enums.JobPriority;
import com.scheduler.enums.JobStatus;
import com.scheduler.repository.DeadLetterJobRepository;
import com.scheduler.repository.JobRepository;
import com.scheduler.service.dlq.exception.DlqAlreadyRequeuedException;
import com.scheduler.service.dlq.exception.DlqEntryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterJobServiceImpl implements DeadLetterJobService {

    private final DeadLetterJobRepository deadLetterJobRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional
    public DeadLetterJob moveToDLQ(Job job, String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        UUID sourceId = job.getId();

        return deadLetterJobRepository.findByJobIdAndRequeuedIsFalse(sourceId)
                .map(existing -> {
                    existing.setErrorMessage(errorMessage);
                    existing.setFailedAt(now);
                    existing.setRetryCount(job.getRetryCount());
                    log.info(
                            "Updated existing DLQ entry id={} for jobId={} (idempotent move)",
                            existing.getId(),
                            sourceId
                    );
                    return deadLetterJobRepository.save(existing);
                })
                .orElseGet(() -> {
                    DeadLetterJob row = DeadLetterJob.builder()
                            .jobId(sourceId)
                            .jobName(job.getName())
                            .cronExpression(job.getCronExpression())
                            .maxRetries(job.getMaxRetries())
                            .payload(job.getPayload())
                            .errorMessage(errorMessage)
                            .retryCount(job.getRetryCount())
                            .failedAt(now)
                            .requeued(false)
                            .createdAt(now)
                            .build();
                    DeadLetterJob saved = deadLetterJobRepository.save(row);
                    log.info("Moved job to DLQ: dlqId={}, jobId={}, jobName={}", saved.getId(), sourceId, job.getName());
                    return saved;
                });
    }

    @Override
    @Transactional
    public Job requeueFromDLQ(Long dlqId) {
        DeadLetterJob dlq = deadLetterJobRepository.findById(dlqId)
                .orElseThrow(() -> new DlqEntryNotFoundException(dlqId));
        return performRequeue(dlq);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeadLetterJob> findAllDlqEntries() {
        return deadLetterJobRepository.findAllByOrderByFailedAtDesc();
    }

    @Override
    @Transactional
    public List<DlqRequeueOutcome> requeueAllPending() {
        List<DeadLetterJob> pending = deadLetterJobRepository.findByRequeuedFalseOrderByFailedAtAsc();
        List<DlqRequeueOutcome> outcomes = new ArrayList<>(pending.size());
        for (DeadLetterJob dlq : pending) {
            Long dlqId = dlq.getId();
            Job newJob = performRequeue(dlq);
            outcomes.add(new DlqRequeueOutcome(dlqId, newJob));
        }
        log.info("Bulk requeue completed: count={}", outcomes.size());
        return outcomes;
    }

    /**
     * Creates a new ACTIVE job, marks the DLQ row requeued. Invoked only from transactional entry points
     * ({@link #requeueFromDLQ}, {@link #requeueAllPending}) so persistence is consistent.
     */
    private Job performRequeue(DeadLetterJob dlq) {
        if (dlq.isRequeued()) {
            throw new DlqAlreadyRequeuedException(dlq.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        Job newJob = Job.builder()
                .name(dlq.getJobName())
                .cronExpression(dlq.getCronExpression())
                .status(JobStatus.ACTIVE)
                .priority(JobPriority.MEDIUM)
                .retryCount(0)
                .maxRetries(dlq.getMaxRetries())
                .payload(dlq.getPayload())
                .nextExecutionTime(now)
                .build();

        Job saved = jobRepository.save(newJob);

        dlq.setRequeued(true);
        deadLetterJobRepository.save(dlq);

        log.info(
                "Requeued from DLQ: dlqId={}, newJobId={}, name={}",
                dlq.getId(),
                saved.getId(),
                saved.getName()
        );
        return saved;
    }
}
