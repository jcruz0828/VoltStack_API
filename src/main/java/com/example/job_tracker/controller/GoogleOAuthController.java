package com.example.job_tracker.controller;

import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${api.base.path}/auth/google")
@RequiredArgsConstructor
public class GoogleOAuthController {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private static final String SCOPE = String.join(" ",
            "openid",
            "email",
            "profile",
            "https://www.googleapis.com/auth/gmail.readonly"
    );

    @GetMapping("/login")
    public void googleLogin(HttpServletResponse response) throws Exception {
        String state = UUID.randomUUID().toString();
        String oauthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent" +
                "&state=" + state;

        response.sendRedirect(oauthUrl);
    }

    @GetMapping("/callback")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws Exception {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        String tokenRequestBody =
                "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                        "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                        "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                        "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                        "&grant_type=authorization_code";

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<InputStream> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofInputStream());
        JsonNode tokenJson = objectMapper.readTree(tokenResponse.body());

        if (tokenJson.has("error")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google token error: " + tokenJson.path("error").asText());
            return;
        }

        String accessToken = tokenJson.path("access_token").asText(null);
        String refreshToken = tokenJson.path("refresh_token").asText(null);
        long expiresIn = tokenJson.path("expires_in").asLong(0);
        Instant expiry = Instant.now().plusSeconds(expiresIn);

        if (accessToken == null || expiresIn == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google OAuth failed: missing token fields.");
            return;
        }

        HttpRequest profileReq = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        HttpResponse<InputStream> profileRes = client.send(profileReq, HttpResponse.BodyHandlers.ofInputStream());
        JsonNode profileJson = objectMapper.readTree(profileRes.body());

        if (profileJson.has("error")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google userinfo error: " + profileJson.path("error").path("message").asText());
            return;
        }

        String email = profileJson.path("email").asText(null);

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google OAuth failed: missing email in profile.");
            return;
        }

        User user = Optional.ofNullable(userRepository.findByEmail(email)).orElse(new User());
        user.setEmail(email);
        user.setGoogleAccessToken(accessToken);
        user.setGoogleRefreshToken(refreshToken);
        user.setGoogleTokenExpiry(expiry);
        userRepository.save(user);

        response.sendRedirect("/success.html");
    }
}
