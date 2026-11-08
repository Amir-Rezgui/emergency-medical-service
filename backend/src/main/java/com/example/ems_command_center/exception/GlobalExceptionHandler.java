package com.example.ems_command_center.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("code", determineErrorCode(ex.getMessage()));
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        HttpStatus status = determineStatus(ex.getMessage());
        return new ResponseEntity<>(body, status);
    }

    private String determineErrorCode(String message) {
        if (message.contains("not found")) return "MISSION_NOT_FOUND";
        if (message.contains("already assigned")) return "MISSION_ALREADY_ASSIGNED";
        if (message.contains("already completed")) return "MISSION_ALREADY_COMPLETED";
        if (message.contains("Invalid status transition")) return "INVALID_STATUS_TRANSITION";
        return "INTERNAL_ERROR";
    }

    private HttpStatus determineStatus(String message) {
        if (message.contains("not found")) return HttpStatus.NOT_FOUND;
        if (message.contains("already assigned")) return HttpStatus.CONFLICT;
        if (message.contains("already completed")) return HttpStatus.BAD_REQUEST;
        if (message.contains("Invalid status transition")) return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
