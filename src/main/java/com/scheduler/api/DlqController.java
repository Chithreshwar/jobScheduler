package com.scheduler.api;

import com.scheduler.api.dto.DlqBulkRequeueResponse;
import com.scheduler.api.dto.DlqEntryResponse;
import com.scheduler.api.dto.DlqRequeueResponse;
import com.scheduler.domain.DeadLetterJob;
import com.scheduler.domain.Job;
import com.scheduler.service.dlq.DeadLetterJobService;
import com.scheduler.service.dlq.DlqRequeueOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dlq")
@RequiredArgsConstructor
public class DlqController {

    private final DeadLetterJobService deadLetterJobService;

    @GetMapping
    public ResponseEntity<List<DlqEntryResponse>> listDlqEntries() {
        List<DeadLetterJob> entries = deadLetterJobService.findAllDlqEntries();
        List<DlqEntryResponse> body = entries.stream()
                .map(DlqEntryResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{id}/requeue")
    public ResponseEntity<DlqRequeueResponse> requeue(@PathVariable("id") Long id) {
        Job newJob = deadLetterJobService.requeueFromDLQ(id);
        log.info("DLQ requeue via API: dlqId={}, newJobId={}", id, newJob.getId());
        DlqRequeueResponse response = DlqRequeueResponse.of(id, newJob);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/requeue-all")
    public ResponseEntity<DlqBulkRequeueResponse> requeueAll() {
        List<DlqRequeueOutcome> outcomes = deadLetterJobService.requeueAllPending();
        List<DlqBulkRequeueResponse.RequeuedItem> items = outcomes.stream()
                .map(o -> DlqBulkRequeueResponse.RequeuedItem.builder()
                        .dlqId(o.dlqId())
                        .newJobId(o.newJob().getId())
                        .build())
                .toList();
        log.info("DLQ requeue-all via API: requeuedCount={}", items.size());
        DlqBulkRequeueResponse body = DlqBulkRequeueResponse.builder()
                .requeuedCount(items.size())
                .items(items)
                .build();
        HttpStatus status = items.isEmpty() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
