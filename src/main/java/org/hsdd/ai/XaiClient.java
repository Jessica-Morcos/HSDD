package org.hsdd.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class XaiClient implements AiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String apiKey;
    private final String url;
    private final String model;

    public XaiClient(
            @Value("${xai.api-key}") String apiKey,
            @Value("${xai.url}") String url,
            @Value("${xai.model}") String model) {
        this.apiKey = apiKey;
        this.url = url;
        this.model = model;
    }

    @Override
    public Result analyze(String text, List<String> tags) {
        try {
            // 1) Build messages
            Map<String, Object> systemMsg = Map.of(
                    "role", "system",
                    "content", """
                        You are a medical triage assistant.
                        You MUST ALWAYS answer ONLY with a JSON object:
                        {"label": "<short best-guess diagnosis>", "confidence": <number between 0 and 1>}
                        Never say "unknown", "needs_review", or similar.
                        Pick your BEST GUESS even if information is incomplete.
                        """
            );

            String userContent = """
                Symptoms description:
                %s

                Tags: %s

                Respond ONLY with JSON like:
                {"label": "migraine", "confidence": 0.91}
                """.formatted(text, tags);

            Map<String, Object> userMsg = Map.of(
                    "role", "user",
                    "content", userContent
            );

            // 2) Request body
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("stream", false);
            body.put("temperature", 0);
            body.put("messages", List.of(systemMsg, userMsg));

            // 3) Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // 4) Call Grok
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, entity, String.class);

            // 5) Parse JSON generically (NO XaiChatResponse)
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode firstChoice = root.path("choices").get(0);
            String content = firstChoice
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            // 6) Try to parse Grok's content as JSON { "label": ..., "confidence": ... }
            try {
                JsonNode node = objectMapper.readTree(content);
                String label = node.path("label").asText();
                double conf = node.path("confidence").asDouble(0.5);

                if (label == null || label.isBlank()) {
                    label = "model_response";
                }

                return new Result(label, conf);
            } catch (JsonProcessingException e) {
                // If Grok returns plain text instead of JSON, use that as label
                return new Result(content, 0.5);
            }

        } catch (Exception e) {
            throw new RuntimeException("xAI call failed", e);
        }
    }
}