package com.example.job_tracker.service.groq;

import com.example.job_tracker.service.groq.iGroqLlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqLLMService implements iGroqLlmService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Override
    public String detectStatus(String from, String subject, String body) {
        String prompt = """
                You are an email classifier for engineering job applications. Classify this email into **exactly one** of these categories ONLY:
                OFFER, TECHNICAL_OFFER, INTERNSHIP_OFFER, TECHNICAL_INTERVIEW, CODING_CHALLENGE,
                SYSTEM_DESIGN, BEHAVIORAL, ONSITE, REJECTED, TECHNICAL_REJECT, NO_POSITION,
                PENDING, ON_HOLD, FOLLOW_UP, UNRELATED.

                - If the email is not related to jobs, recruiting, interviews, or employment, choose UNRELATED.
                - If you are unsure or the email is ambiguous, choose UNRELATED.
                - If the input is empty, choose UNRELATED.
                - Do not make up data or invent categories.
                - Output **exactly** one line: CATEGORY:SCORE (e.g., INTERNSHIP_OFFER:0.97). Do not add extra text, explanation, or commentary.
                - CATEGORY must be one of the above only. SCORE must be a decimal between 0 and 1.

                Email:
                From: %s
                Subject: %s
                Body: %s
                """.formatted(from, subject, body);

        try {
            JsonNode response = callGroqAPI(prompt);
            String output = safeExtractContent(response);
            if (output == null) return "PENDING";
            return output.split(":")[0];
        } catch (Exception e) {
            log.error("detectStatus failed", e);
            return "PENDING";
        }
    }

    @Override
    public List<String> extractCompanies(String subject, String body) {
        String prompt = """
                Extract all real company names mentioned in the following email. Return only a JSON array of company names, for example: ["Google", "Tesla"].
                - If no company names are found, return [].
                - Do not make up or invent companies.
                - Return ONLY the JSON array, nothing else, no explanation.

                Subject: %s
                Body: %s
                """.formatted(subject, body);

        try {
            JsonNode response = callGroqAPI(prompt);
            String raw = safeExtractContent(response);
            if (raw == null) return Collections.emptyList();
            JsonNode companyList = objectMapper.readTree(raw);
            List<String> companies = new ArrayList<>();
            companyList.forEach(node -> companies.add(node.asText()));
            return companies;
        } catch (Exception e) {
            log.error("extractCompanies failed", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isJobRelated(String subject, String body) {
        String prompt = """
                Determine if the following email is about job applications, recruiting, interviews, hiring, or employment.
                - If it is not about these topics, return NO.
                - If you are unsure or the input is ambiguous, return NO.
                - Do not make up context. If the input is empty, return NO.
                - Return ONLY YES or NO. No extra words, no explanation, no punctuation.

                Subject: %s
                Body: %s
                """.formatted(subject, body);

        try {
            JsonNode response = callGroqAPI(prompt);
            String result = safeExtractContent(response);
            log.info("LLM raw output for isJobRelated: subject='{}' body='{}' --> {}", subject, body, result); // Debug log
            if (result == null) return false;
            result = result.trim().toUpperCase();
            return result.equals("YES");
        } catch (Exception e) {
            log.error("isJobRelated failed", e);
            return false;
        }
    }

    // Debug helper to get raw output for prompt analysis
    public String isJobRelatedRaw(String subject, String body) {
        String prompt = """
                Determine if the following email is about job applications, recruiting, interviews, hiring, or employment.
                - If it is not about these topics, return NO.
                - If you are unsure or the input is ambiguous, return NO.
                - Do not make up context. If the input is empty, return NO.
                - Return ONLY YES or NO. No extra words, no explanation, no punctuation.

                Subject: %s
                Body: %s
                """.formatted(subject, body);

        try {
            JsonNode response = callGroqAPI(prompt);
            String result = safeExtractContent(response);
            log.info("isJobRelatedRaw: subject='{}' body='{}' --> '{}'", subject, body, result); // Extra debug
            return result;
        } catch (Exception e) {
            log.error("isJobRelatedRaw failed", e);
            return null;
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
        String respBody = Objects.requireNonNull(response.body()).string();

        JsonNode root = objectMapper.readTree(respBody);

        // Extra: Log Groq errors in API response
        if (root.has("error")) {
            log.error("Groq API error: {}", root.get("error").toPrettyString());
        }

        return root;
    }

    /**
     * Safely extracts the content string from a Groq/OpenAI-like API response,
     * or returns null if not present.
     */
    private String safeExtractContent(JsonNode root) {
        if (root == null) return null;
        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode messageNode = choices.get(0).get("message");
            if (messageNode != null && messageNode.has("content")) {
                return messageNode.get("content").asText();
            }
        }
        if (root.has("error")) {
            log.error("Groq error in response: {}", root.get("error").toPrettyString());
        } else {
            log.error("Unexpected Groq response: {}", root.toPrettyString());
        }
        return null;
    }
}
