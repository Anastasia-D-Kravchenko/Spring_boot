package com.eventflow.service;

import com.eventflow.config.GroqProperties;
import com.eventflow.config.EventFlowProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnthropicService {

    private final WebClient anthropicWebClient;
    private final GroqProperties anthropicProperties;
    private final EventFlowProperties eventFlowProperties;
    private final ObjectMapper objectMapper;

    public Map<String, Object> chat(String userMessage, String context) {
        String systemPrompt = buildSystemPrompt(context);

        // Groq requires the OpenAI-compatible "messages" format
        Map<String, Object> requestBody = Map.of(
                "model", anthropicProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.0 // Deterministic for reliability
        );

        try {
            String responseJson = anthropicWebClient.post()
                    .uri("/chat/completions") // Groq endpoint
                    .header("Authorization", "Bearer " + anthropicProperties.getKey())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseJson);
            // Path adjusted for Groq/OpenAI response structure
            String aiText = root.path("choices").get(0).path("message").path("content").asText();

            String cleaned = aiText.trim();
            if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
            if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();

            return objectMapper.readValue(cleaned, Map.class);

        } catch (WebClientResponseException e) {
            log.error("Groq API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Map.of("action", "ERROR", "message", "Could not connect to AI service.", "data", Map.of());
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            return Map.of("action", "INFO", "message", "Error parsing response.", "data", Map.of());
        }
    }

    private String buildSystemPrompt(String context) {
        String base = eventFlowProperties.getAi().getSystemPrompt();
        return (context != null && !context.isBlank()) ? base + "\n\nCurrent context:\n" + context : base;
    }
}