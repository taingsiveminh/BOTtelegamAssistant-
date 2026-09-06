package com.example.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram")
@Data
public class TelegramConfig {
    private String botToken;
    private String botUsername = "MyAiAssistantBot";
    private String mode = "polling"; // "polling" or "webhook"
    private String webhookUrl;
    private String secretToken = "secret_token_123";
    private long maxFileSizeBytes = 10485760; // 10MB
}
