package com.intelliflow.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {

    /**
     * Google AI Gemini API key. Prefer {@code GEMINI_API_KEY} env var in production.
     * Sent as query parameter {@code key}; not used as an Authorization header.
     */
    private String apiKey = "";

    /**
     * Full generateContent endpoint URL (without {@code ?key=}); used to derive {@link #getBaseUrl()}
     * and {@link #getRequestPath()} for {@link org.springframework.web.reactive.function.client.WebClient}.
     */
    private String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /** Scheme + authority for WebClient {@code baseUrl} (e.g. {@code https://generativelanguage.googleapis.com}). */
    public String getBaseUrl() {
        URI u = URI.create(url.trim());
        return u.getScheme() + "://" + u.getAuthority();
    }

    /** Path for the generateContent call (e.g. {@code /v1beta/models/gemini-1.5-flash:generateContent}). */
    public String getRequestPath() {
        return URI.create(url.trim()).getPath();
    }
}
