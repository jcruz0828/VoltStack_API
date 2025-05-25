package com.example.job_tracker.controller;

import com.example.job_tracker.service.email.EmailTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base.path}/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailTrackingService emailTrackingService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncEmails() {
        emailTrackingService.fetchAndProcessEmails();
        return ResponseEntity.ok("Email sync complete");
    }
}
