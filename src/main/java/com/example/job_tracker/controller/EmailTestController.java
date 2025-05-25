package com.example.job_tracker.controller;

import com.example.job_tracker.service.email.EmailTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.base.path}/test-email")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailTrackingService emailTrackingService;

    @GetMapping("/subjects")
    public List<String> fetchSubjects(@RequestParam String email) {
        return emailTrackingService.fetchJobRelatedEmailSubjectsForUser(email);
    }
}
