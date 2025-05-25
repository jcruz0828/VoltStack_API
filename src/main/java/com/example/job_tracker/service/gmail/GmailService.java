package com.example.job_tracker.service.gmail;

import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GmailService {

    private static final String APPLICATION_NAME = "Job Tracker Email Ingestion";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    private final UserRepository userRepository;

    public Gmail buildServiceForUser(User user) throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .build()
                .setAccessToken(user.getGoogleAccessToken())
                .setRefreshToken(user.getGoogleRefreshToken())
                .setExpirationTimeMilliseconds(user.getGoogleTokenExpiry().toEpochMilli());

        if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
            if (credential.refreshToken()) {
                user.setGoogleAccessToken(credential.getAccessToken());
                user.setGoogleTokenExpiry(Instant.now().plusSeconds(credential.getExpiresInSeconds()));
                userRepository.save(user);
            }
        }

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Message> fetchUnreadMessages(User user) throws Exception {
        Gmail service = buildServiceForUser(user);
        ListMessagesResponse response = service.users().messages()
                .list("me")
                .setQ("is:unread category:primary")
                .setMaxResults(10L)
                .execute();
        return response.getMessages();
    }

    public Message getMessageDetail(User user, String messageId) throws Exception {
        Gmail service = buildServiceForUser(user);
        return service.users().messages().get("me", messageId).setFormat("FULL").execute();
    }
}
