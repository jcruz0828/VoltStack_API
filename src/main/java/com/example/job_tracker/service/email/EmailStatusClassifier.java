package com.example.job_tracker.service.email;

import org.springframework.stereotype.Component;

@Component
public class EmailStatusClassifier {

    public String detectStatus(String body) {
        String text = body.toLowerCase();
        if (text.contains("interview")) return "interview";
        if (text.contains("unfortunately") || text.contains("regret")) return "rejected";
        if (text.contains("congratulations") || text.contains("offer")) return "offer";
        return "pending";
    }
}
