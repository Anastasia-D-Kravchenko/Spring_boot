package com.eventflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("groq.api")
@Data
public class GroqProperties {
    private String key;
    private String baseUrl = "https://api.groq.com/openai/v1";
    private String model = "llama-3.1-8b-instant";
    private int maxTokens = 2048;
}