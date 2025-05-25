package com.example.job_tracker.service.groq;

import com.example.job_tracker.service.groq.iGroqLlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GroqLLMService implements iGroqLlmService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Override
    public String detectStatus(String from, String subject, String body) {
        String prompt = """
                You are an email classifier for engineering job applications. Classify this email into exactly one of these categories:
                OFFER, TECHNICAL_OFFER, INTERNSHIP_OFFER, TECHNICAL_INTERVIEW, CODING_CHALLENGE,
                SYSTEM_DESIGN, BEHAVIORAL, ONSITE, REJECTED, TECHNICAL_REJECT, NO_POSITION,
                PENDING, ON_HOLD, FOLLOW_UP, UNRELATED.

                Email:
                From: %s
                Subject: %s
                Body: %s

                Return only the category name and confidence score (0-1) in this format: CATEGORY:SCORE
                """.formatted(from, subject, body);

        try {
            JsonNode response = callGroqAPI(prompt);
            String output = response.get("choices").get(0).get("message").get("content").asText().trim();
            return output.split(":")[0];
        } catch (Exception e) {
            e.printStackTrace();
            return "PENDING";
        }
    }

    @Override
    public List<String> extractCompanies(String subject, String body) {
        String prompt = """
                Extract all company names mentioned in the following email.
                Return only a JSON array of company names, e.g. [\"Google\", \"Tesla\"].

                Subject: %s
                Body: %s
                """.formatted(subject, body);

        try {
            JsonNode response = callGroqAPI(prompt);
            String raw = response.get("choices").get(0).get("message").get("content").asText().trim();
            JsonNode companyList = objectMapper.readTree(raw);
            List<String> companies = new ArrayList<>();
            companyList.forEach(node -> companies.add(node.asText()));
            return companies;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isJobRelated(String subject, String body) {
        String prompt = """
                Determine if the following email is related to a job application, recruiting, interview, hiring, or employment.

                Email:
                Subject: %s
                Body: %s

                Return only YES or NO.
                """.formatted(subject, body);

        try {
            JsonNode response = callGroqAPI(prompt);
            String result = response.get("choices").get(0).get("message").get("content").asText().trim().toUpperCase();
            return result.equals("YES");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private JsonNode callGroqAPI(String prompt) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String jsonBody = objectMapper.writeValueAsString(Map.of(
                "model", "meta-llama/llama-4-scout-17b-16e-instruct",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.1,
                "top_p", 0.1,
                "max_tokens", 100
        ));

        Request request = new Request.Builder()
                .url(GROQ_URL)
                .addHeader("Authorization", "Bearer " + groqApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        return objectMapper.readTree(Objects.requireNonNull(response.body()).string());
    }
}

