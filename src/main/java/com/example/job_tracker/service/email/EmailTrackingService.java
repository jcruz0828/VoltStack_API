package com.example.job_tracker.service.email;

import com.example.job_tracker.enums.JobStatus;
import com.example.job_tracker.model.Application;
import com.example.job_tracker.model.EmailMetadata;
import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.ApplicationRepository;
import com.example.job_tracker.repository.EmailMetadataRepository;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.service.groq.GroqLLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailTrackingService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailMetadataRepository emailMetadataRepository;
    private final GroqLLMService  groqLLMService;

    public void fetchAndProcessEmails() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<SimulatedEmail> inbox = getMockInboxForUser(user);

            for (SimulatedEmail email : inbox) {
                // Filter unrelated emails
                if (!groqLLMService.isJobRelated(email.subject(), email.body())) continue;

                String detectedStatus = groqLLMService.detectStatus(email.from(), email.subject(), email.body());
                Application matchingApp = findMatchingApplication(user, email.body());

                EmailMetadata metadata = new EmailMetadata();
                metadata.setFromAddress(email.from());
                metadata.setSubject(email.subject());
                metadata.setBodySnippet(email.body().substring(0, Math.min(255, email.body().length())));
                metadata.setDetectedStatus(detectedStatus);
                metadata.setReceivedAt(LocalDateTime.now());
                metadata.setUser(user);
                metadata.setApplication(matchingApp);
                emailMetadataRepository.save(metadata);

                if (matchingApp != null && detectedStatus != null) {
                    matchingApp.setJobStatus(parseJobStatus(detectedStatus));
                    applicationRepository.save(matchingApp);
                }
            }
        }
    }

    private Application findMatchingApplication(User user, String emailBody) {
        return user.getJobs().stream()
                .filter(app -> emailBody.toLowerCase().contains(app.getCompany().getName().toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    private JobStatus parseJobStatus(String status) {
        if (status == null || status.isBlank()) return JobStatus.PENDING;

        return switch (status.toUpperCase()) {
            // OFFER variants
            case "OFFER", "TECHNICAL_OFFER", "INTERNSHIP_OFFER", "RETURN_OFFER", "FINAL_OFFER" -> JobStatus.OFFER;

            // ACCEPTED variants
            case "OFFER_ACCEPTED", "SIGNED", "STARTED", "HIRED" -> JobStatus.ACCEPTED;

            // REJECTED variants
            case "REJECTED", "TECHNICAL_REJECT", "NO_POSITION", "DECLINED", "AUTO_REJECTED", "SOFT_REJECTED" -> JobStatus.REJECTED;

            // INTERVIEW variants
            case "INTERVIEW", "TECHNICAL_INTERVIEW", "CODING_CHALLENGE", "SYSTEM_DESIGN", "BEHAVIORAL", "ONSITE", "PHONE_SCREEN", "HR_INTERVIEW" -> JobStatus.INTERVIEW;

            // FOLLOW_UP
            case "FOLLOW_UP", "REMINDER", "CHECKING_IN", "REENGAGED" -> JobStatus.FOLLOW_UP;

            // ON_HOLD
            case "ON_HOLD", "PAUSED", "POSITION_DELAYED", "FROZEN_PIPELINE" -> JobStatus.ON_HOLD;

            // APPLIED
            case "APPLIED", "APPLICATION_RECEIVED", "SUBMITTED", "RESUME_REVIEW" -> JobStatus.APPLIED;

            // ARCHIVED
            case "SPAM", "UNRELATED", "DEAD", "WITHDRAWN", "ARCHIVED" -> JobStatus.ARCHIVED;

            // Default to pending
            default -> JobStatus.PENDING;
        };
    }

    private record SimulatedEmail(String from, String subject, String body) {}

    private List<SimulatedEmail> getMockInboxForUser(User user) {
        return List.of(
                new SimulatedEmail("recruiter@company.com", "Interview Scheduled", "We’d like to invite you to interview at Tesla."),
                new SimulatedEmail("hr@company.com", "Unfortunately", "We regret to inform you…")
        );
    }
}
