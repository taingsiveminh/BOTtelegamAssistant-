package com.example.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {
    private String provider = "openai";
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o-mini";
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private int memoryMaxMessages = 10;
    private int memorySummaryThreshold = 20;
}
