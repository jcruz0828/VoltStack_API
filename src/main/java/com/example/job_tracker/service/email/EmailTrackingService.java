package com.example.job_tracker.service.email;

import com.example.job_tracker.enums.JobStatus;
import com.example.job_tracker.model.Application;
import com.example.job_tracker.model.EmailMetadata;
import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.ApplicationRepository;
import com.example.job_tracker.repository.EmailMetadataRepository;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.service.groq.GroqLLMService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmailTrackingService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailMetadataRepository emailMetadataRepository;
    private final GroqLLMService groqLLMService;

    // --- Original processing logic ---
    public void fetchAndProcessEmails() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getGoogleAccessToken() == null) continue;

            try {
                Gmail gmail = buildGmailClient(user);

                ListMessagesResponse messagesResponse = gmail.users().messages().list("me")
                        .setMaxResults(10L)
                        .setQ("") // Add query if needed
                        .execute();

                List<Message> messages = messagesResponse.getMessages();
                if (messages == null) continue;

                for (Message messageMeta : messages) {
                    Message message = gmail.users().messages().get("me", messageMeta.getId())
                            .setFormat("full")
                            .execute();

                    String subject = "";
                    String from = "";
                    String body = "";

                    for (var header : message.getPayload().getHeaders()) {
                        if (header.getName().equalsIgnoreCase("Subject")) {
                            subject = header.getValue();
                        } else if (header.getName().equalsIgnoreCase("From")) {
                            from = header.getValue();
                        }
                    }
                    if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
                        body = new String(Base64.getUrlDecoder().decode(message.getPayload().getBody().getData()), StandardCharsets.UTF_8);
                    } else if (message.getPayload().getParts() != null) {
                        for (var part : message.getPayload().getParts()) {
                            if (part.getMimeType().equals("text/plain") && part.getBody() != null && part.getBody().getData() != null) {
                                body = new String(Base64.getUrlDecoder().decode(part.getBody().getData()), StandardCharsets.UTF_8);
                                break;
                            }
                        }
                    }

                    // Filter non-job-related emails
                    if (!groqLLMService.isJobRelated(subject, body)) continue;

                    String detectedStatus = groqLLMService.detectStatus(from, subject, body);
                    Application matchingApp = findMatchingApplication(user, body);

                    EmailMetadata metadata = new EmailMetadata();
                    metadata.setFromAddress(from);
                    metadata.setSubject(subject);
                    metadata.setBodySnippet(body.substring(0, Math.min(255, body.length())));
                    metadata.setDetectedStatus(detectedStatus);
                    metadata.setReceivedAt(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(message.getInternalDate()), ZoneOffset.UTC
                    ));
                    metadata.setUser(user);
                    metadata.setApplication(matchingApp);
                    emailMetadataRepository.save(metadata);

                    if (matchingApp != null && detectedStatus != null) {
                        matchingApp.setJobStatus(parseJobStatus(detectedStatus));
                        applicationRepository.save(matchingApp);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // --- New: TESTING FUNCTION to fetch and display email subjects for a user ---
    public List<String> fetchJobRelatedEmailSubjectsForUser(String userEmail) {
        List<String> emailSummaries = new ArrayList<>();
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByEmail(userEmail));
        if (optionalUser.isEmpty()) {
            emailSummaries.add("User not found: " + userEmail);
            return emailSummaries;
        }

        User user = optionalUser.get();
        if (user.getGoogleAccessToken() == null) {
            emailSummaries.add("No Google access token for user: " + userEmail);
            return emailSummaries;
        }

        try {
            Gmail gmail = buildGmailClient(user);
            ListMessagesResponse messagesResponse = gmail.users().messages().list("me")
                    .setMaxResults(20L)
                    .setQ("")
                    .execute();

            List<Message> messages = messagesResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                emailSummaries.add("No messages found.");
                return emailSummaries;
            }

            for (Message messageMeta : messages) {
                Message message = gmail.users().messages().get("me", messageMeta.getId())
                        .setFormat("full")
                        .execute();

                String subject = "";
                String from = "";
                String body = "";




                for (var header : message.getPayload().getHeaders()) {
                    if (header.getName().equalsIgnoreCase("Subject")) {
                        subject = header.getValue();
                    } else if (header.getName().equalsIgnoreCase("From")) {
                        from = header.getValue();
                    }
                }
                if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
                    body = new String(Base64.getUrlDecoder().decode(message.getPayload().getBody().getData()), StandardCharsets.UTF_8);
                } else if (message.getPayload().getParts() != null) {
                    for (var part : message.getPayload().getParts()) {
                        if (part.getMimeType().equals("text/plain") && part.getBody() != null && part.getBody().getData() != null) {
                            body = new String(Base64.getUrlDecoder().decode(part.getBody().getData()), StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }

                // Filter non-job-related emails using your Groq classifier
                if (!groqLLMService.isJobRelated(subject, body)) continue;
                List<String> company = groqLLMService.extractCompanies(subject,body);

                // Optionally: Get a label/classification, too
                String status = groqLLMService.detectStatus(from, subject, body);

                emailSummaries.add("From: " + from + " | Subject: " + subject + " | Status: " + status + " | " + company);
            }
        } catch (Exception ex) {
            emailSummaries.add("Error: " + ex.getMessage());
        }

        return emailSummaries;
    }


    // --- Gmail client builder (for a user) ---
    private Gmail buildGmailClient(User user) throws Exception {
        Credential credential = new GoogleCredential().setAccessToken(user.getGoogleAccessToken());
        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Job Tracker App").build();
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
            case "OFFER", "TECHNICAL_OFFER", "INTERNSHIP_OFFER", "RETURN_OFFER", "FINAL_OFFER" -> JobStatus.OFFER;
            case "OFFER_ACCEPTED", "SIGNED", "STARTED", "HIRED" -> JobStatus.ACCEPTED;
            case "REJECTED", "TECHNICAL_REJECT", "NO_POSITION", "DECLINED", "AUTO_REJECTED", "SOFT_REJECTED" -> JobStatus.REJECTED;
            case "INTERVIEW", "TECHNICAL_INTERVIEW", "CODING_CHALLENGE", "SYSTEM_DESIGN", "BEHAVIORAL", "ONSITE", "PHONE_SCREEN", "HR_INTERVIEW" -> JobStatus.INTERVIEW;
            case "FOLLOW_UP", "REMINDER", "CHECKING_IN", "REENGAGED" -> JobStatus.FOLLOW_UP;
            case "ON_HOLD", "PAUSED", "POSITION_DELAYED", "FROZEN_PIPELINE" -> JobStatus.ON_HOLD;
            case "APPLIED", "APPLICATION_RECEIVED", "SUBMITTED", "RESUME_REVIEW" -> JobStatus.APPLIED;
            case "SPAM", "UNRELATED", "DEAD", "WITHDRAWN", "ARCHIVED" -> JobStatus.ARCHIVED;
            default -> JobStatus.PENDING;
        };
    }
}
