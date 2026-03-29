package com.intelliflow.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GeminiFailureAnalyzer implements AIFailureAnalyzer {

    private final WebClient geminiWebClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    public GeminiFailureAnalyzer(
            @Qualifier("geminiWebClient") WebClient geminiWebClient,
            GeminiProperties geminiProperties,
            ObjectMapper objectMapper) {
        this.geminiWebClient = geminiWebClient;
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public AIFailureResponse analyzeFailure(String errorMessage, String stackTrace, String jobType) {
        if (geminiProperties.getApiKey() == null || geminiProperties.getApiKey().isBlank()) {
            log.warn("Gemini API key is not configured (gemini.api-key)");
            return fallback(
                    "Gemini API key is not configured",
                    "Set gemini.api-key or GEMINI_API_KEY environment variable.",
                    false,
                    "HIGH"
            );
        }

        String promptText = buildPromptText(errorMessage, stackTrace, jobType);

        Map<String, Object> body = Map.of(
                "contents",
                List.of(
                        Map.of(
                                "parts",
                                List.of(
                                        Map.of("text", promptText)
                                )
                        )
                )
        );

        try {
            String responseJson = geminiWebClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path(geminiProperties.getRequestPath())
                            .queryParam("key", geminiProperties.getApiKey())
                            .build())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String assistantText = extractCandidateText(responseJson);
            String jsonPayload = stripMarkdownFence(assistantText);
            return objectMapper.readValue(jsonPayload, AIFailureResponse.class);
        } catch (WebClientResponseException e) {
            log.error(
                    "Gemini API error: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            return fallback(
                    "Gemini request failed: HTTP " + e.getStatusCode(),
                    safeBodySnippet(e.getResponseBodyAsString()),
                    false,
                    "HIGH"
            );
        } catch (Exception e) {
            log.error("Failed to analyze failure with Gemini", e);
            return fallback(
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(),
                    "Check logs and Gemini API availability.",
                    false,
                    "HIGH"
            );
        }
    }

    private static String buildPromptText(String errorMessage, String stackTrace, String jobType) {
        return """
                You are an expert backend engineer.
                Analyze the following job failure.

                Error: %s
                StackTrace: %s
                JobType: %s

                Return ONLY valid JSON in this format:
                {
                  "rootCause": "...",
                  "suggestion": "...",
                  "shouldRetry": true,
                  "severity": "LOW"
                }

                Use shouldRetry true or false. Use severity one of: LOW, MEDIUM, HIGH.
                """.formatted(
                nullToEmpty(errorMessage),
                nullToEmpty(stackTrace),
                nullToEmpty(jobType)
        );
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String extractCandidateText(String responseJson) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (text.isMissingNode() || text.asText().isBlank()) {
            throw new IllegalStateException("Gemini response missing candidates[0].content.parts[0].text");
        }
        return text.asText();
    }

    private static String stripMarkdownFence(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNl = trimmed.indexOf('\n');
            if (firstNl > 0) {
                trimmed = trimmed.substring(firstNl + 1);
            }
            int fence = trimmed.lastIndexOf("```");
            if (fence > 0) {
                trimmed = trimmed.substring(0, fence);
            }
            return trimmed.trim();
        }
        return trimmed;
    }

    private static AIFailureResponse fallback(String rootCause, String suggestion, boolean shouldRetry, String severity) {
        return new AIFailureResponse(rootCause, suggestion, shouldRetry, severity);
    }

    private static String safeBodySnippet(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}
