package com.example.job_tracker.controller;

import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${api.base.path}/auth/google")
@RequiredArgsConstructor
public class GoogleOAuthController {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GoogleOAuthController.class);


    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private static final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";

    @GetMapping("/login")
    public void googleLogin(HttpServletResponse response) throws Exception {
        String state = UUID.randomUUID().toString();
        String oauthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + SCOPE +
                "&access_type=offline" +
                "&prompt=consent" +
                "&state=" + state;

        log.info("Redirecting to Google OAuth URL: {}", oauthUrl);
        response.sendRedirect(oauthUrl);
    }

    @GetMapping("/callback")
    public String googleCallback(@RequestParam("code") String code) throws Exception {
        log.info("Received callback with code: {}", code);

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "code=" + code +
                                "&client_id=" + clientId +
                                "&client_secret=" + clientSecret +
                                "&redirect_uri=" + redirectUri +
                                "&grant_type=authorization_code"))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<InputStream> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofInputStream());
        JsonNode tokenJson = objectMapper.readTree(tokenResponse.body());

        log.info("Token response: {}", tokenJson.toPrettyString());

        String accessToken = tokenJson.get("access_token").asText();
        String refreshToken = tokenJson.has("refresh_token") ? tokenJson.get("refresh_token").asText() : null;
        long expiresIn = tokenJson.get("expires_in").asLong();
        Instant expiry = Instant.now().plusSeconds(expiresIn);

        HttpRequest profileReq = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        HttpResponse<InputStream> profileRes = client.send(profileReq, HttpResponse.BodyHandlers.ofInputStream());
        JsonNode profileJson = objectMapper.readTree(profileRes.body());

        log.info("User profile: {}", profileJson.toPrettyString());

        String email = profileJson.get("email").asText();

        User user = Optional.ofNullable(userRepository.findByEmail(email)).orElse(new User());
        user.setEmail(email);
        user.setGoogleAccessToken(accessToken);
        user.setGoogleRefreshToken(refreshToken);
        user.setGoogleTokenExpiry(expiry);
        userRepository.save(user);

        log.info("User {} saved/updated in DB", email);
        return "redirect:/success.html";
    }
}
