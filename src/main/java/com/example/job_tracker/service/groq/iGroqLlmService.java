package com.example.job_tracker.service.groq;

import java.util.List;

public interface iGroqLlmService {
    String detectStatus(String from, String subject, String body);
    List<String> extractCompanies(String subject, String body);
    boolean isJobRelated(String subject, String body);
}
