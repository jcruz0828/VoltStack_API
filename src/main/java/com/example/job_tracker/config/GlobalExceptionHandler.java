package com.example.job_tracker.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.ResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception e) {
        System.out.println(">>> Caught exception: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(500).body("Internal error: " + e.getMessage());
    }
}
