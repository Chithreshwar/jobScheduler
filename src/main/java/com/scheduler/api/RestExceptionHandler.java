package com.scheduler.api;

import com.scheduler.api.dto.ApiErrorResponse;
import com.scheduler.service.dlq.exception.DlqAlreadyRequeuedException;
import com.scheduler.service.dlq.exception.DlqEntryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DlqEntryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDlqNotFound(DlqEntryNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DlqAlreadyRequeuedException.class)
    public ResponseEntity<ApiErrorResponse> handleDlqAlreadyRequeued(DlqAlreadyRequeuedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(ex.getMessage()));
    }
}
